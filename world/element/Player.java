package world.element;

import java.util.List;

import helper.Config;
import helper.Logger;
import server.WorldServer;
import user.User;

public class Player extends Movable {
	public Player(Config config, Logger logger) {
		super(config, logger);
	}

	@Override
	public void destroy(Config config, Logger logger, WorldServer worldServer) {
		return;
	}

	@Override
	// checks if colliding with Enemy and kills them if so
	public void tick(Config config, Logger logger, WorldServer worldServer) {
		List<Movable> collisionMovableS = collision.collisionsGet(worldServer.characterList, position, this,
				(WorldElement worldElementRelative, Movable that) -> {
					return that instanceof Enemy;
				});
		// death
		if (collisionMovableS.size() != 0) {
			owner.state = User.State.Dead;
		}
	}
}
