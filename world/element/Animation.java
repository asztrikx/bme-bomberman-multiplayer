package world.element;

import java.awt.Image;
import java.io.Serializable;

import di.DI;

public class Animation implements Serializable {
	private static AnimationStore animationStore = (AnimationStore) DI.get(AnimationStore.class);

	// Current image id to be displayed
	private long state = 0;
	// Delay state
	private long stateDelayTick = 0;
	// Last delay state == how long should it delay between image changes
	public long stateDelayTickEnd;
	// Last image id to be displayed
	public long stateEnd;

	public String path;

	public Animation(final long stateDelayTickEnd, final String path) {
		this.stateDelayTickEnd = stateDelayTickEnd;
		this.path = path;
		this.stateEnd = animationStore.get(path).size();
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
		state %= animationStore.get(path).size();
	}

	public void reset() {
		state = 0;
		stateDelayTick = 0;
	}

	public Image getImage() {
		return animationStore.get(path).get((int) state);
	}
}
