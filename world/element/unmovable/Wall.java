package world.element.unmovable;

import helper.Config;
import helper.Logger;
import server.WorldServer;

public class Wall extends Unmovable {

	@Override
	public void destroy(Config config, Logger logger, WorldServer worldServer) {
		return;
	}
}
