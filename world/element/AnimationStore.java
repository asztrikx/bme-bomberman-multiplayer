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
	Map<String, List<Image>> classNameFrames = new HashMap<>();

	public void addPath(String path) throws IOException {
		File file = new File(path);
		File[] classNames = file.listFiles();
		for (File className : classNames) {
			List<Image> frames = new ArrayList<>();
			classNameFrames.put(className.getName(), frames);

			File[] frameFiles = className.listFiles();
			for (File frame : frameFiles) {
				Image image = new ImageIcon(frame.getCanonicalPath()).getImage();
				frames.add(image);
			}
		}
	}

	public List<Image> get(String className) {
		return classNameFrames.get(className);
	}
}
