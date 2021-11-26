package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

	public void connect(final Function<Connection, Boolean> handshake, final Consumer<Object> receive, String ip,
			int port) throws Exception {
		this.receive = receive;

		try {
			final Socket socket = new Socket(ip, port);
			final InputStream inputStream = socket.getInputStream();
			// ObjectInputStream has to be first as server has to send ObjectXXStream header
			// first
			final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			final OutputStream outputStream = socket.getOutputStream();
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			connection = new Connection(objectInputStream, objectOutputStream, socket);
		} catch (final IOException e) {
			logger.printf("Couldn't connect to %s:%d\n", ip, port);
			return;
		}

		if (!handshake.apply(connection)) {
			connection.close();
			return;
		}

		phaser.register();
		thread = new Thread(new Receive());
		thread.start();
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
