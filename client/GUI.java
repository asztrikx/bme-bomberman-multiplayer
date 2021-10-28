package client;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import client.KeyCapturePanel.KeyMap;
import di.DI;
import helper.Config;
import helper.Key;

public class GUI {
	private static Config config = (Config) DI.services.get(Config.class);

	public Draw draw = new Draw();
	public JFrame jFrame;
	public KeyCapturePanel panel;

	public enum State {
		Lobby, Ingame,
	}

	private State state = State.Lobby;

	public GUI(Runnable connect, Runnable disconnect, Runnable send, boolean[] keys) {
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
		panel = new KeyCapturePanel(keyMaps, keys, () -> {
			send.run();
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
					disconnect.run();
				}

				config.ip = cols[0];
				config.port = Integer.parseInt(cols[1]);
				if (config.name.equals("")) {
					config.name = config.defaultName;
				}
				try {
					connect.run();
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		jMenu.add(jMenuItem);

		JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem("Auto reconnect");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				config.autoreconnect = jCheckBoxMenuItem.getState();
			}
		});
		jMenu.add(jCheckBoxMenuItem);

		jMenuItem = new JMenuItem("Disconnect");
		jMenuItem.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (state != State.Ingame) {
					JOptionPane.showMessageDialog(jFrame, "Not in game");
					return;
				}

				disconnect.run();
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

	public void setState(State state) {
		switch (state) {
			case Ingame:
				jFrame.add(panel);
				jFrame.setVisible(true);

				// control handle
				// - only after connected
				panel.add(draw);
				panel.active = true;

				// show after all elements are added
				panel.setVisible(true);
				jFrame.setVisible(true);

				// jframe has to be visible before draw added
				draw.init();

				break;
			case Lobby:
				panel.active = false;
				panel.remove(draw);
				panel.setVisible(false);
				break;
			default:
				break;
		}

		this.state = state;
	}
}
