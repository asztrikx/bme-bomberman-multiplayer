package world.element;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

public class AnimationStore {
	private Map<String, List<Image>> framesByPath = new HashMap<>();

	private void add(String path) {
		try {
			File file = new File(path);
			File[] frameFiles = file.listFiles();

			List<Image> frames = new ArrayList<>();
			for (File frame : frameFiles) {
				Image image = new ImageIcon(frame.getCanonicalPath()).getImage();
				frames.add(image);
			}
			framesByPath.put(path, frames);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Image> get(String path) {
		if (!framesByPath.containsKey(path)) {
			add(path);
		}
		return framesByPath.get(path);
	}
}
