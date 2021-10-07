package world.element;

import java.io.Serializable;

import helper.Position;
import server.WorldServer;

public abstract class WorldElement implements Serializable {
	public Position position = new Position(0, 0);
	public Animation animation;
	public long createdTick = -1;
	public long destroyTick = -1;

	public WorldElement(Animation animation) {
		this.animation = animation;
	}

	public boolean shouldDestroy(long tickCount) {
		return tickCount == destroyTick;
	}

	public void destroy(WorldServer worldServer, WorldServer nextWorldServer, long tickCount) {

	}

	public void nextState(WorldServer worldServer, WorldServer nextWorldServer, long tickCount) {
	}
}
