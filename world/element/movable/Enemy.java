package world.element.movable;

import java.security.SecureRandom;

import di.DI;
import helper.Config;
import helper.Key;
import helper.Position;
import server.WorldServer;
import world.element.Animation;

public class Enemy extends Movable {
	private static Config config = (Config) DI.get(Config.class);

	public Enemy() {
		super(new Animation(10, "resource/movable/enemy"));
	}

	/**
	 * @formatter:off
	 * Moves to new position
	 * Decides whether it should change direction to move in next tick
	 * @formatter:on
	 */
	@Override
	public void nextState(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		// move
		super.nextState(worldServer, nextWorldServer, tickCount);

		// decide to change direction
		final SecureRandom secureRandom = new SecureRandom();
		if (secureRandom.nextDouble() > config.enemyKeyChangePossibility) {
			return;
		}
		randomKeys();
	}

	/**
	 * If could not move change direction
	 */
	@Override
	public void applyMovement(WorldServer worldServer, WorldServer nextWorldServer, long tickCount) {
		Position positionCurrent = position;
		super.applyMovement(worldServer, nextWorldServer, tickCount);
		if (position.equals(positionCurrent)) {
			randomKeys();
		}
	}

	/**
	 * @formatter:off
	 * Randomizes the keys array
	 * @formatter:on
	 */
	public void randomKeys() {
		final SecureRandom secureRandom = new SecureRandom();
		// roll new direction
		for (int i = 0; i < Key.KeyType.KeyLength; i++) {
			keys[i] = false;
		}
		keys[secureRandom.nextInt(Key.KeyType.KeyLength)] = true;
	}
}
