package world.element;

import java.security.SecureRandom;

import helper.Config;
import helper.Key;
import helper.Logger;
import server.WorldServer;

public class Enemy extends Movable {
	public Enemy(Logger logger, Config config) {
		super(config, logger);
	}

	@Override
	public void move(WorldServer worldServer, long tickCount) {
		super.move(worldServer, tickCount);

		// roll new direction after move
		SecureRandom secureRandom = new SecureRandom();
		for (int i = 0; i < Key.KeyType.KeyLength; i++) {
			keys[i] = false;
		}
		keys[secureRandom.nextInt(Key.KeyType.KeyLength)] = true;

	}
}
