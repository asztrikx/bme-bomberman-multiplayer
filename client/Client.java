package client;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import di.DI;
import helper.AutoClosableLock;
import helper.Config;
import helper.Logger;
import network.Connect;
import network.Network.Connection;
import user.User;

public class Client implements AutoCloseable {
	private static Config config = (Config) DI.services.get(Config.class);
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	private UserClient userClient = new UserClient();
	private Connect connect;
	private ReentrantLock lock = new ReentrantLock();
	private boolean active;
	private GUI gui = new GUI(() -> {
		try {
			connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}, () -> disconnect(), () -> send(), userClient.keys);

	public void connect() throws Exception {
		userClient.name = config.name;
		active = true;

		// connect
		connect = new Connect();
		connect.connect((Connection connection) -> {
			try {
				handshake();
			} catch (ClassNotFoundException | IOException e) {
				logger.println("Couldn't handshake:");
				logger.println(e);
				return false;
			}

			gui.setState(GUI.State.Ingame);

			return true;
		}, (Object object) -> {
			receive(object);
		});
	}

	private boolean handshake() throws IOException, ClassNotFoundException {
		// send name
		connect.send(userClient.name);

		// receive new name, auth
		User user = (User) connect.receive();

		// apply changes
		// - name could be occupied
		// TODO string len limits
		userClient.auth = user.auth;
		userClient.name = user.name;

		return true;
	}

	// gets updates from server
	private void receive(Object object) {
		WorldClient worldClient = (WorldClient) object;
		gui.draw.setWorldClient(worldClient);
		if (!gui.draw.render()) {
			// otherwise this would wait for a deregister which would happend after this
			// line finished
			new Thread(() -> disconnect()).start();
		}
	}

	// Sends userClient to server as UserServer
	private void send() {
		try {
			connect.send((User) userClient);
		} catch (IOException e) {
			logger.println("Client couldn't send update");
		}
	}

	private void disconnect() {
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			if (!active) {
				return;
			}

			active = false;

			gui.setState(GUI.State.Lobby);
			try {
				connect.close();
			} catch (Exception e) {
				throw new RuntimeException();
			}
		}
	}

	@Override
	public void close() throws Exception {
		disconnect();
	}

	public void waitUntilWin() {
		while (true) {
			// TODO get state
		}
	}
}
