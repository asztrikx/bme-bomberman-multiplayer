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
	private final Map<String, List<Image>> framesByPath = new HashMap<>();

	private void add(final String path) {
		try {
			final File file = new File(path);
			final File[] frameFiles = file.listFiles();

			final List<Image> frames = new ArrayList<>();
			for (final File frame : frameFiles) {
				final Image image = new ImageIcon(frame.getCanonicalPath()).getImage();
				frames.add(image);
			}
			framesByPath.put(path, frames);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Image> get(final String path) {
		if (!framesByPath.containsKey(path)) {
			add(path);
		}
		return framesByPath.get(path);
	}
}
