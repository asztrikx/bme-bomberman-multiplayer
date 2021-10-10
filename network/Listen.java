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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import di.DI;
import helper.AutoClosableLock;
import helper.Logger;

public class Listen extends Network {
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	// lock for
	// - connections
	// - serverSocket
	public Lock lock = new ReentrantLock();
	public List<Connection> connections;
	private ServerSocket serverSocket;

	public int port;

	private Function<Connection, Boolean> handshake;
	private BiConsumer<Connection, Object> receive;
	private Consumer<Connection> disconnect;
	private final Phaser phaser = new Phaser(0);

	public void listen(int port, Function<Connection, Boolean> handshake, BiConsumer<Connection, Object> receive,
			Consumer<Connection> disconnect) {
		connections = new LinkedList<>();
		this.port = port;
		this.handshake = handshake;
		this.receive = receive;
		this.disconnect = disconnect;

		// blocking accept => new thread
		phaser.register();
		Thread thread = new Thread(new Handshake());
		thread.start();
	}

	private class Receive implements Runnable {
		private Connection connection;

		public Receive(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			// receive will block so there's no point in locking (it would starve other
			// threads), just handle exceptions as close
			while (!serverSocket.isClosed()) {
				try {
					Object object = receive(connection.objectInputStream);
					Listen.this.receive.accept(connection, object);
				} catch (ClassNotFoundException | IOException e) {
					disconnect();
					break;
				}
			}

			phaser.arriveAndDeregister();
		}

		public void disconnect() {
			try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
				// disconnect first to prevent using connection after close but before
				// disconnect()
				disconnect.accept(connection);

				connection.close();
				connections.remove(connection);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class Handshake implements Runnable {
		@Override
		public void run() {
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e1) {
				throw new Error(e1);
			}

			while (!serverSocket.isClosed()) {
				Socket socket;
				try {
					socket = serverSocket.accept();
				} catch (IOException e1) {
					// serverSocket closed
					if (serverSocket.isClosed()) {
						break;
					} else {
						logger.printf("Socket accept failed");
						e1.printStackTrace();
						continue;
					}
				}

				// do not stop if a clients fails to connect => try here
				// server might be stopping => closing sockets => lock before accepting new
				try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
					// between this and previous block lock could have been used
					if (serverSocket.isClosed()) {
						break;
					}

					// ObjectOutputStream has to be first as server has to send ObjectXXStream
					// header first
					OutputStream outputStream = socket.getOutputStream();
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
					InputStream inputStream = socket.getInputStream();
					ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
					Connection connection = new Connection(objectInputStream, objectOutputStream, socket);
					connections.add(connection);

					if (handshake.apply(connection)) {
						logger.printf("Handshake with server successful %s\n", connection.toString());

						phaser.register();
						Thread thread = new Thread(new Receive(connection));
						thread.start();
					} else {
						logger.printf("Handshake with server failed %s\n", connection.toString());

						socket.close();
					}
				} catch (Exception e) {
					logger.printf("Client failed to connect: %s:%d\n", Network.getIP(socket), Network.getPort(socket));
					e.printStackTrace();
				}
			}

			// has to be outside of exception handle to always decrement
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public void close() throws Exception {
		// do not let new sockets to be added to list
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			// only close one of objectOutputStream, objectInputStream
			for (Connection connection : connections) {
				connection.close();
			}
			serverSocket.close();
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
