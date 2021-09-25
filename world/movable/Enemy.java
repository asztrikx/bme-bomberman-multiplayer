package world.movable;

import java.security.SecureRandom;

import helper.Config;
import helper.Key;
import server.WorldServer;
import world.element.Animation;

public class Enemy extends Movable {
	private Config config = Config.Injected;

	public Enemy() {
		super(new Animation(10, Enemy.class.getSimpleName()));
	}

	@Override
	public void move(WorldServer worldServer, long tickCount) {
		// move
		super.move(worldServer, tickCount);

		// decide to change direction
		SecureRandom secureRandom = new SecureRandom();
		if (secureRandom.nextDouble() > config.enemyKeyChangePossibility) {
			return;
		}
		randomKeys();
	}

	public void randomKeys() {
		SecureRandom secureRandom = new SecureRandom();
		// roll new direction
		for (int i = 0; i < Key.KeyType.KeyLength; i++) {
			keys[i] = false;
		}
		keys[secureRandom.nextInt(Key.KeyType.KeyLength)] = true;
	}

	@Override
	public void destroy(WorldServer worldServer) {
	}

	@Override
	public void tick(WorldServer worldServer) {
	}
}
