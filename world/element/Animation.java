package world.element;

public class Animation {
	public long state;
	public long stateDelayTick;
	public long stateDelayTickEnd;

	public Animation(long state, long stateDelayTick, long stateDelayTickEnd) {
		this.state = state;
		this.stateDelayTick = stateDelayTick;
		// TODO def is 2
		this.stateDelayTickEnd = stateDelayTickEnd;
	}
}
