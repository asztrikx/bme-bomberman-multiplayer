package world.element;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class Animation {
	// TODO private
	public long state = 0;
	public long stateDelayTick = 0;
	public long stateDelayTickEnd;

	public List<Image> images = new ArrayList<>();
	public AnimationStore animationStore; // TODO inject
	public String className;

	public Animation(long stateDelayTickEnd, String className) {
		// TODO def is 2
		this.stateDelayTickEnd = stateDelayTickEnd;
		this.className = className;
	}

	public void increase() {
		// delay
		stateDelayTick++;
		if (stateDelayTick <= stateDelayTickEnd) {
			return;
		}
		stateDelayTick = 0;

		// state next
		state++;
		state %= animationStore.get(className).size();
	}
}
