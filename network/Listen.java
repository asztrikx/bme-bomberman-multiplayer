package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import di.DI;
import helper.AutoClosableLock;
import helper.Logger;

public class Listen extends Network {
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	// lock for
	// - sockets
	// - active
	public Lock lock = new ReentrantLock();
	public boolean active;
	public List<Connection> connections;

	public int port;

	public Function<Connection, Boolean> handshake;
	public Consumer<Object> receive;
	private final Phaser phaser = new Phaser(0);

	public void listen(int port, Function<Connection, Boolean> handshake, Consumer<Object> receive) {
		connections = new LinkedList<>();
		this.port = port;
		this.active = true;
		this.handshake = handshake;
		this.receive = receive;

		// blocking accept => new thread
		phaser.register();
		Thread thread = new Thread(new Handshake());
		thread.start();
	}

	private class Receive implements Runnable {
		Connection connection;

		public Receive(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			lock.lock();
			while (active) {
				lock.unlock();

				try {
					Object object = receive(connection.objectInputStream);
					Listen.this.receive.accept(object);
				} catch (ClassNotFoundException | IOException e) {
					String ip = Network.getIP(connection.socket);
					int port = Network.getPort(connection.socket);
					logger.printf("Couldn't receive client: %s:%d\n", ip, port);
					logger.println(e.getStackTrace());
				}

				lock.lock();
			}

			phaser.arriveAndDeregister();
		}
	}

	private class Handshake implements Runnable {
		@Override
		public void run() {
			try {
				try (ServerSocket serverSocket = new ServerSocket(port)) {
					lock.lock();
					while (active) {
						lock.unlock();
						Socket socket = serverSocket.accept();

						// do not stop if a clients fails to connect => try here
						try {
							// server might be stopping => closing sockets => lock before accepting new
							try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
								OutputStream outputStream = socket.getOutputStream();
								ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
								InputStream inputStream = socket.getInputStream();
								ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
								Connection connection = new Connection(objectInputStream, objectOutputStream, socket);
								connections.add(connection);

								if (active && handshake.apply(connection)) {
									phaser.register();
									Thread thread = new Thread(new Receive(connection));
									thread.start();
								} else {
									socket.close();
								}
							}
						} catch (Exception e) {
							logger.printf("Client failed to connect: %s\n", e.toString());
						}

						lock.lock();
					}
				}
			} catch (Exception e) {
				throw new Error(e);
			}

			phaser.arriveAndDeregister();
		}
	}

	@Override
	public void close() throws Exception {
		// do not let new sockets to be added to list
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			// only close one of objectOutputStream, objectInputStream
			for (Connection connection : connections) {
				connection.socket.close();
			}
			active = false;
		}
		phaser.awaitAdvance(phaser.getPhase());
	}

	public void send(Object... objects) throws IOException {
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			for (Connection connection : connections) {
				super.send(connection.objectOutputStream, objects);
			}
		}
	}
}
