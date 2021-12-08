package world.element.unmovable;

import java.util.List;

import di.DI;
import engine.Collision;
import helper.Config;
import helper.Position;
import server.WorldServer;
import world.element.Animation;

public class Bomb extends Unmovable {
	private static Config config = (Config) DI.get(Config.class);

	public Bomb() {
		super(new Animation(15, "resource/unmovable/bomb"));
	}

	/**
	 * @formatter:off
	 * Inserts fire on the adjacent blocks
	 * Gives back bomb to Player
	 * @formatter:on
	 */
	@Override
	public void destroy(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		// fire inserts
		final int directionX[] = { 0, 1, -1, 0, 0 };
		final int directionY[] = { 0, 0, 0, 1, -1 };
		for (int j = 0; j < directionX.length; j++) {
			for (int scalar = 1; scalar < config.fireMaxSpread; scalar++) {
				final Position positionFire = new Position(position.y + scalar * directionY[j] * config.squaresize,
						position.x + scalar * directionX[j] * config.squaresize);

				final List<Unmovable> collisionUnmovableS = Collision.getCollisions(worldServer.unmovables,
						positionFire, this, null);
				final boolean boxExists = collisionUnmovableS.stream().filter(t -> t instanceof Box).count() != 0;
				// Wall, etc is hit => don't place fire
				if (!boxExists && collisionUnmovableS.size() != 0) {
					break;
				}

				final Unmovable fire = new BombFire();
				fire.movedOutOfBomb = true;
				fire.createdTick = tickCount;
				fire.destroyTick = tickCount + (long) (0.25 * config.tickSecond);
				fire.owner = owner;
				fire.position = positionFire;
				fire.animation.stateDelayTickEnd = 2;
				fire.velocity = 0;

				nextWorldServer.unmovables.add(fire);

				++scalar;
				// Middle position can't spread
				if (j == 0) {
					break;
				}
				// Box reached don't spread further
				if (boxExists) {
					break;
				}
			}
		}

		// give back bomb to user
		if (owner != null) {
			owner.bombCount++;
		}
	}
}
