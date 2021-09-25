package world.element;

import helper.Position;
import server.WorldServer;

public abstract class WorldElement {
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

	public abstract void destroy(WorldServer worldServer);

	public abstract void tick(WorldServer worldServer);

}
