package world.element.movable;

import java.security.SecureRandom;

import di.DI;
import helper.Config;
import helper.Key;
import server.WorldServer;
import world.element.Animation;

public class Enemy extends Movable {
	private static Config config = (Config) DI.services.get(Config.class);

	public Enemy() {
		super(new Animation(10, "resource/movable/enemy"));
	}

	@Override
	public void nextState(WorldServer worldServer, WorldServer nextWorldServer, long tickCount) {
		// move
		super.nextState(worldServer, nextWorldServer, tickCount);

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
}
