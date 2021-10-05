package network;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;
import java.util.function.Function;

import di.DI;
import helper.Logger;

public class Connect extends Network {
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	private Socket socket;
	private Consumer<Object> receive;
	private final Phaser phaser = new Phaser(0);

	public void connect(String ip, int port, Function<Socket, Boolean> handshake, Consumer<Object> receive) {
		this.receive = receive;

		try {
			socket = new Socket(ip, port);
		} catch (IOException e) {
			logger.printf("Couldn't connect to %s:%d\n", ip, port);
			return;
		}

		handshake.apply(socket);

		phaser.register();
		Thread thread = new Thread(new Receive());
		thread.start();
	}

	private class Receive implements Runnable {
		@Override
		public void run() {
			// TODO not inf
			while (true) {
				try {
					Object object = receive(socket);
					receive.accept(object);
				} catch (ClassNotFoundException | IOException e) {
					// logger.println("received object is wrong:");
				}
			}

			// phaser.arriveAndDeregister();
		}
	}

	public void send(Object... objects) throws IOException {
		super.send(socket, objects);
	}

	public Object receive() throws ClassNotFoundException, IOException {
		return super.receive(socket);
	}

	@Override
	public void close() throws Exception {
		socket.close();
		phaser.arriveAndAwaitAdvance();
	}
}
