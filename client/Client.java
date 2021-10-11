package client;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import client.KeyCapturePanel.KeyMap;
import di.DI;
import helper.AutoClosableLock;
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
	private ReentrantLock lock = new ReentrantLock();
	private boolean active;

	public enum State {
		Lobby, Ingame,
	}

	private State state = State.Lobby;

	public Client() {
		// gui
		jFrame = new JFrame();
		// add extra height
		jFrame.setSize(config.windowWidth, config.windowHeight + 50);
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
		panel.setSize(config.windowWidth, config.windowHeight);
		panel.active = false;

		// menu
		JMenuBar jMenuBar = new JMenuBar();
		JMenu jMenu;
		JMenuItem jMenuItem;

		// game
		jMenu = new JMenu("Game");
		jMenuBar.add(jMenu);

		jMenuItem = new JMenuItem("Connect");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String address = (String) JOptionPane.showInputDialog(jFrame, "Address (ip:port)", "Connect",
						JOptionPane.PLAIN_MESSAGE, null, null, String.format("%s:%d", config.ip, config.port));
				String[] cols = address.split(":");
				if (address == null || cols[0].length() == 0 || cols[1].length() == 0) {
					JOptionPane.showMessageDialog(jFrame, "Wrong format");
					return;
				}

				if (state == State.Ingame) {
					disconnect();
				}

				config.ip = cols[0];
				config.port = Integer.parseInt(cols[1]);
				if (config.name.equals("")) {
					config.name = config.defaultName;
				}
				try {
					connect();
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		jMenu.add(jMenuItem);

		jMenuItem = new JMenuItem("Disconnect");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (state != State.Ingame) {
					JOptionPane.showMessageDialog(jFrame, "Not in game");
					return;
				}

				disconnect();
			}
		});
		jMenu.add(jMenuItem);

		// server
		jMenu = new JMenu("Server");
		jMenuBar.add(jMenu);

		jMenuItem = new JMenuItem("Start server");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				return;
			}
		});
		jMenu.add(jMenuItem);

		jMenuItem = new JMenuItem("Stop server");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				return;
			}
		});
		jMenu.add(jMenuItem);

		// settings
		jMenu = new JMenu("Settings");
		jMenuBar.add(jMenu);

		jMenuItem = new JMenuItem("Open settings");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Desktop.isDesktopSupported()) {
					try {
						File myFile = new File("Main.java");
						Desktop.getDesktop().open(myFile);
					} catch (IOException e2) {
						throw new RuntimeException(e2);
					}
				}
			}
		});
		jMenu.add(jMenuItem);

		jMenuItem = new JMenuItem("Player name");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				config.name = (String) JOptionPane.showInputDialog(jFrame, "Name", "Connect", JOptionPane.PLAIN_MESSAGE,
						null, null, config.name);
			}
		});
		jMenu.add(jMenuItem);

		jFrame.setJMenuBar(jMenuBar);
		jFrame.setVisible(true);
	}

	public void connect() throws Exception {
		userClient.name = config.name;
		active = true;

		jFrame.add(panel);
		jFrame.setVisible(true);

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

			state = State.Ingame;

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
		if (!draw.render()) {
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

			state = State.Lobby;
			try {
				// TODO this block idk why
				connect.close();
			} catch (Exception e) {
				throw new RuntimeException();
			}

			panel.active = false;
			panel.remove(draw);
			panel.setVisible(false);
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
