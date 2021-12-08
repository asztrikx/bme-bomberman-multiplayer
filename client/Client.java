package client;

import java.io.IOException;

import di.DI;
import helper.Config;
import helper.Logger;
import network.Connect;
import network.Network.Connection;
import user.User;

public class Client {
	private static Config config = (Config) DI.get(Config.class);
	private static Logger logger = (Logger) DI.get(Logger.class);

	private final UserClient userClient = new UserClient();
	private Connect connect;
	private ClientModel model = new ClientModel();
	private final GUI gui = new GUI(this::connect, this::disconnect, this::send, userClient.keys);

	/**
	 * @formatter:off
	 * Create a connection to the address in Config
	 * Updates GUI to reflex in game screen
	 * @return boolean success
	 * @formatter:on
	 */
	public boolean connect() {
		userClient.name = config.name;
		model.active = true;

		// connect
		connect = new Connect();
		return connect.connect((final Connection connection) -> {
			try {
				handshake();
			} catch (ClassNotFoundException | IOException e) {
				logger.println("Couldn't handshake:");
				logger.println(e);
				return false;
			}

			gui.setState(GUI.State.Ingame);

			return true;
		}, (final Object object) -> {
			receive(object);
		}, config.ip, config.port);
	}

	/**
	 * @formatter:off
	 * Handshake used at connecting
	 * @return boolean success
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @formatter:on
	 */
	private boolean handshake() throws IOException, ClassNotFoundException {
		// send name
		connect.send(userClient.name);

		// receive new name, auth
		final User user = (User) connect.receive();

		// apply changes
		// - name could be occupied
		userClient.auth = user.auth;
		userClient.name = user.name;

		return true;
	}

	/**
	 * @formatter:off
	 * Function called when connection receives new data
	 * @param object Received data supplied by connection
	 * @formatter:on
	 */
	private void receive(final Object object) {
		final WorldClient worldClient = (WorldClient) object;
		gui.draw.setWorldClient(worldClient);
		if (worldClient.state != User.State.Playing) {
			// otherwise this would wait for a deregister which would happened after this
			// line finished
			new Thread(() -> {
				disconnect();
				if (config.autoreconnect) {
					try {
						connect();
					} catch (final Exception e) {
						logger.println("Failed to autoreconnect");
					}
				}
			}).start();
		} else {
			gui.draw.render();
		}
	}

	/**
	 * @formatter:off
	 * Updates server about current state (userClient)
	 * If can not send it will auto disconnect
	 * @formatter:on
	 */
	private void send() {
		try {
			connect.send((User) userClient);
		} catch (final IOException e) {
			logger.println("Client couldn't send update");
			disconnect();
		}
	}

	/**
	 * @formatter:off
	 * Disconnects from server
	 * Sets gui to not connected state
	 * @formatter:on
	 */
	private void disconnect() {
		synchronized ((Boolean) model.active) {
			if (!model.active) {
				return;
			}

			logger.println("Disconnecting...");

			model.active = false;

			gui.setState(GUI.State.Lobby);
			try {
				connect.close();
			} catch (final Exception e) {
				throw new RuntimeException();
			}
		}
	}
}
