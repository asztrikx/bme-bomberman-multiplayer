package world.element.unmovable;

import server.WorldServer;
import world.element.Animation;

public class Wall extends Unmovable {
	public Wall() {
		super(new Animation(0, Wall.class.getSimpleName()));
	}

	@Override
	public void destroy(WorldServer worldServer) {
	}

	@Override
	public void tick(WorldServer worldServer) {
	}
}
