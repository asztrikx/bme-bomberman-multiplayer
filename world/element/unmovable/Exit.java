package world.element.unmovable;

import helper.Config;
import helper.Logger;
import server.WorldServer;
import world.element.Animation;

public class Exit extends Unmovable {
	public Exit() {
		super(new Animation(10, Exit.class.getSimpleName()));
	}

	@Override
	public void destroy(Config config, Logger logger, WorldServer worldServer) {
	}

	@Override
	public void tick(Config config, Logger logger, WorldServer worldServer) {
	}
}
