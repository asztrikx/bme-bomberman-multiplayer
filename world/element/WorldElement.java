package world.element;

import java.io.Serializable;

import helper.Position;
import server.WorldServer;

public abstract class WorldElement implements Serializable {
	public Position position = new Position(0, 0);
	public Animation animation;
	public long createdTick = -1;
	public long destroyTick = -1;

	public WorldElement(final Animation animation) {
		this.animation = animation;
	}

	/**
	 * @formatter:off
	 * If destroyTick is the current tick returns true
	 * If destroyTick is lower then current tick (e.g. 0) then it will never be destroyed
	 * @param tickCount
	 * @return
	 * @formatter:on
	 */
	public boolean shouldDestroy(final long tickCount) {
		return tickCount == destroyTick;
	}

	public void destroy(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
	}

	public void nextState(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
	}
}
