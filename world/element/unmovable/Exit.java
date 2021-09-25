package world.element.unmovable;

import server.WorldServer;
import world.element.Animation;

public class Exit extends Unmovable {
	public Exit() {
		super(new Animation(10, Exit.class.getSimpleName()));
	}

	@Override
	public void destroy(WorldServer worldServer) {
	}

	@Override
	public void tick(WorldServer worldServer) {
	}
}
