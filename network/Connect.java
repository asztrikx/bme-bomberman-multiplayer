package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import java.util.function.Function;

import di.DI;
import helper.Logger;

public class Connect extends Network {
	private static Logger logger = (Logger) DI.get(Logger.class);

	private Consumer<Object> receive;
	private final Phaser phaser = new Phaser(0);
	private Connection connection;
	private Thread thread;

	public boolean connect(final Function<Connection, Boolean> handshake, final Consumer<Object> receive, String ip,
			int port) {
		this.receive = receive;

		try {
			final Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 2000);
			final InputStream inputStream = socket.getInputStream();
			// ObjectInputStream has to be first as server has to send ObjectXXStream header
			// first
			final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			final OutputStream outputStream = socket.getOutputStream();
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			connection = new Connection(objectInputStream, objectOutputStream, socket);
		} catch (final IOException e) {
			logger.printf("Couldn't connect to %s:%d\n", ip, port);
			return false;
		}

		if (!handshake.apply(connection)) {
			try {
				connection.close();
			} catch (Exception e) {
				logger.println("Could not close connection after unsuccessful handshake");
			}
			return false;
		}

		phaser.register();
		thread = new Thread(new Receive());
		thread.start();
		return true;
	}

	private class Receive implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					final Object object = receive(connection.objectInputStream);
					receive.accept(object);
				} catch (ClassNotFoundException | IOException e) {
					logger.println("Couldn't receive from server or stream stopped...stopping");
					break;
				}
			}

			phaser.arriveAndDeregister();
		}
	}

	public void send(final Object... objects) throws IOException {
		super.send(connection.objectOutputStream, objects);
	}

	public Object receive() throws ClassNotFoundException, IOException {
		return super.receive(connection.objectInputStream);
	}

	public void join() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws Exception {
		// only close one
		connection.socket.close();
		phaser.awaitAdvance(phaser.getPhase());
	}
}
