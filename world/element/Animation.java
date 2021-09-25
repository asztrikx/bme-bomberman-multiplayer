package world.element;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

public class Animation {
	// TODO private
	public long state;
	public long stateDelayTick;
	public long stateDelayTickEnd;

	public List<Image> images = new ArrayList<>();

	public Animation(long state, long stateDelayTick, long stateDelayTickEnd, String[] paths) {
		this.state = state;
		this.stateDelayTick = stateDelayTick;
		// TODO def is 2
		this.stateDelayTickEnd = stateDelayTickEnd;

		// load frames
		for (String path : paths) {
			// TODO
			Image image = new ImageIcon(path).getImage();
			images.add(image);
		}
	}
}
