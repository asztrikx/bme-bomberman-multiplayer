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
import helper.Config;
import helper.Logger;

public class Connect extends Network {
	private static Logger logger = (Logger) DI.services.get(Logger.class);
	private static Config config = (Config) DI.services.get(Config.class);

	private Consumer<Object> receive;
	private final Phaser phaser = new Phaser(0);
	private Connection connection;
	private boolean active = false;

	public void connect(Function<Connection, Boolean> handshake, Consumer<Object> receive) throws Exception {
		this.receive = receive;

		try {
			Socket socket = new Socket(config.ip, config.port);
			InputStream inputStream = socket.getInputStream();
			// ObjectInputStream has to be first as server has to send ObjectXXStream header
			// first
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			OutputStream outputStream = socket.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			connection = new Connection(objectInputStream, objectOutputStream, socket);
		} catch (IOException e) {
			logger.printf("Couldn't connect to %s:%d\n", config.ip, config.port);
			return;
		}

		if (!handshake.apply(connection)) {
			connection.close();
			return;
		}

		active = true;
		phaser.register();
		Thread thread = new Thread(new Receive());
		thread.start();
	}

	private class Receive implements Runnable {
		@Override
		public void run() {
			while (active) {
				try {
					Object object = receive(connection.objectInputStream);
					receive.accept(object);
				} catch (ClassNotFoundException | IOException e) {
					logger.println("received object is wrong:");
				}
			}

			phaser.arriveAndDeregister();
		}
	}

	public void send(Object... objects) throws IOException {
		super.send(connection.objectOutputStream, objects);
	}

	public Object receive() throws ClassNotFoundException, IOException {
		return super.receive(connection.objectInputStream);
	}

	@Override
	public void close() throws Exception {
		active = false;
		// only close one
		connection.socket.close();
		phaser.awaitAdvance(phaser.getPhase());
	}
}
