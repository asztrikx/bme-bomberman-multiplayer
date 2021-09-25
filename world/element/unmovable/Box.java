package world.element.unmovable;

import server.WorldServer;
import world.element.Animation;

public class Box extends Unmovable {
	public Box() {
		super(new Animation(0, Box.class.getSimpleName()));
	}

	@Override
	public void destroy(WorldServer worldServer) {
	}

	@Override
	public void tick(WorldServer worldServer) {
	}
}
