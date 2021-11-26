package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Phaser;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import di.DI;
import helper.Logger;

public class Listen extends Network {
	private static Logger logger = (Logger) DI.get(Logger.class);

	private ListenModel listenModel = new ListenModel();

	public int port;

	private Function<Connection, Boolean> handshake;
	private BiConsumer<Connection, Object> receive;
	private Consumer<Connection> disconnect;
	private final Phaser phaser = new Phaser(0);

	public void listen(final int port, final Function<Connection, Boolean> handshake,
			final BiConsumer<Connection, Object> receive, final Consumer<Connection> disconnect) {
		listenModel.connections = new LinkedList<>();
		this.port = port;
		this.handshake = handshake;
		this.receive = receive;
		this.disconnect = disconnect;

		// blocking accept => new thread
		phaser.register();
		try {
			listenModel.serverSocket = new ServerSocket(port);
		} catch (final IOException e1) {
			throw new Error(e1);
		}
		final Thread thread = new Thread(this::handshake);
		thread.start();
	}

	private class Receive implements Runnable {
		private final Connection connection;

		public Receive(final Connection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			// receive will block so there's no point in locking (it would starve other
			// threads), just handle exceptions as close
			while (!listenModel.serverSocket.isClosed()) {
				try {
					final Object object = receive(connection.objectInputStream);
					Listen.this.receive.accept(connection, object);
				} catch (ClassNotFoundException | IOException e) {
					disconnect();
					break;
				}
			}

			phaser.arriveAndDeregister();
		}

		public void disconnect() {
			synchronized (listenModel) {
				try {
					// disconnect first to prevent using connection after close but before
					// disconnect()
					disconnect.accept(connection);

					connection.close();
					listenModel.connections.remove(connection);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void handshake() {
		while (!listenModel.serverSocket.isClosed()) {
			Socket socket;
			try {
				socket = listenModel.serverSocket.accept();
			} catch (final IOException e1) {
				// serverSocket closed
				if (listenModel.serverSocket.isClosed()) {
					break;
				} else {
					logger.printf("Socket accept failed");
					e1.printStackTrace();
					continue;
				}
			}

			// do not stop if a clients fails to connect => try here
			// server might be stopping => closing sockets => lock before accepting new
			synchronized (listenModel) {
				// between this and previous block lock could have been used
				if (listenModel.serverSocket.isClosed()) {
					break;
				}

				try {
					// ObjectOutputStream has to be first as server has to send ObjectXXStream
					// header first
					final OutputStream outputStream = socket.getOutputStream();
					final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
					final InputStream inputStream = socket.getInputStream();
					final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
					final Connection connection = new Connection(objectInputStream, objectOutputStream, socket);
					listenModel.connections.add(connection);

					if (handshake.apply(connection)) {
						logger.printf("Handshake with server successful %s\n", connection.toString());

						phaser.register();
						final Thread thread = new Thread(new Receive(connection));
						thread.start();
					} else {
						logger.printf("Handshake with server failed %s\n", connection.toString());

						socket.close();
					}
				} catch (final Exception e) {
					logger.printf("Client failed to connect: %s:%d\n", Network.getIP(socket), Network.getPort(socket));
					e.printStackTrace();
				}
			}
		}

		// has to be outside of exception handle to always decrement
		phaser.arriveAndDeregister();
	}

	@Override
	public void close() throws Exception {
		// do not let new sockets to be added to list
		synchronized (listenModel) {
			// only close one of objectOutputStream, objectInputStream
			for (final Connection connection : listenModel.connections) {
				connection.close();
			}
			listenModel.serverSocket.close();
		}
		phaser.awaitAdvance(phaser.getPhase());
	}

	public void send(final Object... objects) throws IOException {
		synchronized (listenModel) {
			for (final Connection connection : listenModel.connections) {
				super.send(connection.objectOutputStream, objects);
			}
		}
	}
}
