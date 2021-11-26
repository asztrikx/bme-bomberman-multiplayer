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

	@Override
	public void destroy(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		// fire inserts
		final int directionX[] = { 0, 1, -1, 0, 0 };
		final int directionY[] = { 0, 0, 0, 1, -1 };
		for (int j = 0; j < directionX.length; j++) {
			final Position positionFire = new Position(position.y + directionY[j] * config.squaresize,
					position.x + directionX[j] * config.squaresize);

			final List<Unmovable> collisionObjectS = Collision.getCollisions(worldServer.unmovables, positionFire, this,
					null);
			final boolean boxExists = collisionObjectS.isEmpty()
					|| collisionObjectS.stream().filter(t -> t instanceof Box).count() != 0;
			if (!boxExists && collisionObjectS.size() != 0) {
				continue;
			}

			final Unmovable objectFire = new BombFire();
			objectFire.movedOutOfBomb = true;
			objectFire.createdTick = tickCount;
			objectFire.destroyTick = tickCount + (long) (0.25 * config.tickSecond);
			objectFire.owner = owner;
			objectFire.position = positionFire;
			objectFire.animation.stateDelayTickEnd = 2;
			objectFire.velocity = 0;

			nextWorldServer.unmovables.add(objectFire);
		}

		// give back bomb to user
		if (owner != null) {
			owner.bombCount++;
		}
	}
}
