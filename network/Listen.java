package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
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
	public List<Socket> sockets;

	public int port;

	public Function<Socket, Boolean> handshake;
	public Consumer<Object> receive;

	public void listen(int port, Function<Socket, Boolean> handshake, Consumer<Object> receive) {
		sockets = new LinkedList<>();
		this.port = port;
		this.active = true;
		this.handshake = handshake;
		this.receive = receive;

		// blocking accept => new thread
		Thread thread = new Thread(new Handshake());
		thread.start();
	}

	private class Receive implements Runnable {
		Socket socket;

		public Receive(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			lock.lock();
			while (active) {
				lock.unlock();

				try {
					Object object = receive(socket);
					Listen.this.receive.accept(object);
				} catch (ClassNotFoundException | IOException e) {
					String ip = Network.getIP(socket);
					int port = Network.getPort(socket);
					logger.printf("Couldn't receive client: %s:%d\n", ip, port);
					logger.println(e.getStackTrace());
				}

				lock.lock();
			}
		}
	}

	private class Handshake implements Runnable {
		@Override
		public void run() {
			try {
				try (ServerSocket serverSocket = new ServerSocket(port)) {
					Socket socket = serverSocket.accept();

					// do not stop if a clients fails to connect => try here
					try {
						// server might be stopping => closing sockets => lock before accepting new
						try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
							if (active && handshake.apply(socket)) {
								sockets.add(socket);

								Thread thread = new Thread(new Receive(socket));
								thread.start();
							} else {
								socket.close();
							}
						}
					} catch (Exception e) {
						logger.printf("Client failed to connect: %s\n", e.toString());
					}
				}
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	@Override
	public void close() throws Exception {
		// do not let new sockets to be added to list
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			for (Socket socket : sockets) {
				socket.close();
			}
			active = false;
		}
	}

	public void send(Object... objects) throws IOException {
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			for (Socket socket : sockets) {
				super.send(socket, objects);
			}
		}
	}
}
