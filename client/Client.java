package client;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import di.DI;
import helper.Config;
import helper.Key;
import helper.Key.KeyType;
import helper.Logger;
import network.Connect;
import user.User;

public class Client implements AutoCloseable {
	private static Config config = (Config) DI.services.get(Config.class);
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	private UserClient userClient = new UserClient();
	private Draw draw = new Draw();
	private Connect connect;
	private JFrame jFrame;
	private KeyListener keyListener;

	public enum State {
		Lobby, Ingame,
	}

	public Client() {
		// gui
		jFrame = new JFrame();
		jFrame.add(draw);
		jFrame.setSize(config.windowWidth, config.windowHeight);
		jFrame.setVisible(true);
		jFrame.setResizable(false);
	}

	public void connect(String ip, int port, String name) throws UnknownHostException, IOException {
		userClient.name = name;

		// connect
		connect = new Connect();
		connect.connect(ip, port, (Socket socket) -> {
			try {
				return handshake(socket);
			} catch (ClassNotFoundException | IOException e) {
				logger.println("Couldn't handshake:");
				logger.println(e.getStackTrace());
				return false;
			}
		}, (Object object) -> {
			receive(object);
		});

		// control handle
		// - only after connected
		keyListener = new KeyHandler();
		jFrame.addKeyListener(keyListener);
	}

	private class KeyHandler implements KeyListener {
		public Key.KeyType getKeysFromEvent(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
					return KeyType.KeyUp;
				case KeyEvent.VK_D:
					return KeyType.KeyRight;
				case KeyEvent.VK_S:
					return KeyType.KeyDown;
				case KeyEvent.VK_A:
					return KeyType.KeyLeft;
				case KeyEvent.VK_SPACE:
					return KeyType.KeyBomb;
				default:
					return null;
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			logger.println("typed event fired");
		}

		@Override
		public void keyPressed(KeyEvent e) {
			Key.KeyType keyType = getKeysFromEvent(e);
			if (keyType != null) {
				userClient.keys[keyType.getValue()] = true;
				send();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			Key.KeyType keyType = getKeysFromEvent(e);
			if (keyType != null) {
				userClient.keys[keyType.getValue()] = false;
				send();
			}
		}
	}

	private boolean handshake(Socket socket) throws IOException, ClassNotFoundException {
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
		draw.repaint();
	}

	// Sends userClient to server as UserServer
	private void send() {
		try {
			connect.send((User) userClient);
		} catch (IOException e) {
			logger.println("Couldn't send update");
			logger.println(e.getStackTrace());
		}
	}

	@Override
	public void close() throws Exception {
		connect.close();
		jFrame.removeKeyListener(keyListener);
		keyListener = null;
	}

	public void waitUntilWin() {
		while (true) {
			// TODO get state
		}
	}
}
