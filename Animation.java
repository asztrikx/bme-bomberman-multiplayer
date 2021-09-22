public class Animation {
	long state;
	long stateDelayTick;
	long stateDelayTickEnd;

	public Animation(long state, long stateDelayTick, long stateDelayTickEnd) {
		this.state = state;
		this.stateDelayTick = stateDelayTick;
		this.stateDelayTickEnd = stateDelayTickEnd;
	}
}
