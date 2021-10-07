package client;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class KeyCapturePanel extends JPanel {
	public static class KeyMap {
		public int keyEvent;
		public String name;
		public int keysIndex;

		public KeyMap(int keyEvent, String name, int keysIndex) {
			this.keyEvent = keyEvent;
			this.name = name;
			this.keysIndex = keysIndex;
		}
	}

	public boolean active = true;

	public KeyCapturePanel(List<KeyMap> keyMaps, boolean[] keys, Runnable callback) {
		InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();
		boolean[] onreleases = { false, true };
		String[] prefixes = { "press", "release" };
		for (int i = 0; i < onreleases.length; i++) {
			boolean onrelease = onreleases[i];
			String prefix = prefixes[i] + ".";

			for (KeyMap keyMap : keyMaps) {
				inputMap.put(KeyStroke.getKeyStroke(keyMap.keyEvent, 0, onrelease), prefix + keyMap.name);
				actionMap.put(prefix + keyMap.name, new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO thread safe
						if (!active) {
							return;
						}
						if (onrelease) {
							keys[keyMap.keysIndex] = false;
						} else {
							keys[keyMap.keysIndex] = true;
						}
						callback.run();
					}
				});
			}
		}
	}
}
