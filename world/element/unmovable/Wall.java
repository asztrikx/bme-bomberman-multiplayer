package world.element.unmovable;

import helper.Config;
import helper.Logger;
import server.WorldServer;
import world.element.Animation;

public class Wall extends Unmovable {
	public Wall() {
		super(new Animation(0, Wall.class.getSimpleName()));
	}

	@Override
	public void destroy(Config config, Logger logger, WorldServer worldServer) {
	}

	@Override
	public void tick(Config config, Logger logger, WorldServer worldServer) {
	}
}
