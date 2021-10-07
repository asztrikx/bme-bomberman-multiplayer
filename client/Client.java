package client;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import client.KeyCapturePanel.KeyMap;
import di.DI;
import helper.Config;
import helper.Key;
import helper.Logger;
import network.Connect;
import network.Network.Connection;
import user.User;

public class Client implements AutoCloseable {
	private static Config config = (Config) DI.services.get(Config.class);
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	private UserClient userClient = new UserClient();
	private Draw draw = new Draw();
	private Connect connect;
	private JFrame jFrame;
	// private KeyListener keyListener;
	private KeyCapturePanel panel;

	public enum State {
		Lobby, Ingame,
	}

	public Client() {
		// gui
		jFrame = new JFrame();
		jFrame.setSize(config.windowWidth, config.windowHeight);
		jFrame.setResizable(false);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<KeyMap> keyMaps = new ArrayList<>();
		keyMaps.add(new KeyMap(KeyEvent.VK_W, "up", Key.KeyType.KeyUp.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_D, "right", Key.KeyType.KeyRight.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_S, "down", Key.KeyType.KeyDown.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_A, "left", Key.KeyType.KeyLeft.getValue()));
		keyMaps.add(new KeyMap(KeyEvent.VK_SPACE, "bomb", Key.KeyType.KeyBomb.getValue()));
		panel = new KeyCapturePanel(keyMaps, userClient.keys, () -> {
			send();
		});
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.active = false;

		panel.setBackground(new Color(30, 30, 30));

		jFrame.getContentPane().add(panel);
	}

	public void connect(String ip, int port, String name) throws UnknownHostException, IOException {
		userClient.name = name;

		// connect
		connect = new Connect();
		connect.connect(ip, port, (Connection connection) -> {
			try {
				handshake();
			} catch (ClassNotFoundException | IOException e) {
				logger.println("Couldn't handshake:");
				logger.println(e);
				return false;
			}

			// control handle
			// - only after connected
			panel.add(draw);
			panel.active = true;

			// show after all elements are added
			panel.setVisible(true);
			jFrame.setVisible(true);

			// jframe has to be visible before draw added
			draw.init();

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
		draw.setWorldClient(worldClient);
		draw.render();
	}

	// Sends userClient to server as UserServer
	private void send() {
		try {
			connect.send((User) userClient);
		} catch (IOException e) {
			logger.println("Couldn't send update");
		}
	}

	@Override
	public void close() throws Exception {
		panel.active = false;
		panel.remove(draw);
		panel.setVisible(false);
		jFrame.remove(panel);
		jFrame.setVisible(false);
		connect.close();
	}

	public void waitUntilWin() {
		while (true) {
			// TODO get state
		}
	}
}
