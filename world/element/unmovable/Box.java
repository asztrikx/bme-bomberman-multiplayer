package world.element.unmovable;

import helper.Config;
import helper.Logger;
import server.WorldServer;
import world.element.Animation;

public class Box extends Unmovable {
	public Box() {
		super(new Animation(0, Box.class.getSimpleName()));
	}

	@Override
	public void destroy(Config config, Logger logger, WorldServer worldServer) {
	}

	@Override
	public void tick(Config config, Logger logger, WorldServer worldServer) {
	}
}
