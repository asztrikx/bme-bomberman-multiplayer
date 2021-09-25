package world.element.unmovable;

import java.util.List;

import engine.Collision;
import helper.Config;
import helper.Position;
import server.WorldServer;
import world.element.Animation;

public class Bomb extends Unmovable {
	private Config config = Config.Injected;
	private Collision collision = Collision.Injected;

	public Bomb() {
		super(new Animation(15, Bomb.class.getSimpleName()));
	}

	@Override
	public void destroy(WorldServer worldServer) {
		long tickCount = destroyTick;

		// fire inserts
		int directionX[] = { 0, 1, -1, 0, 0 };
		int directionY[] = { 0, 0, 0, 1, -1 };
		for (int j = 0; j < directionX.length; j++) {
			Position positionFire = new Position(position.y + directionY[j] * config.squaresize,
					position.x + directionX[j] * config.squaresize);

			List<Unmovable> collisionObjectS = collision.getCollisions(worldServer.unmovables, positionFire, this,
					null);
			boolean boxExists = collisionObjectS.isEmpty()
					|| collisionObjectS.stream().filter(t -> t instanceof Box).count() != 0;
			if (!boxExists && collisionObjectS.size() != 0) {
				continue;
			}

			Unmovable objectFire = new BombFire();
			objectFire.bombOut = true;
			objectFire.createdTick = tickCount;
			objectFire.destroyTick = tickCount + (long) (0.25 * config.tickSecond);
			objectFire.owner = owner;
			objectFire.position = positionFire;
			objectFire.animation.stateDelayTickEnd = 2;
			objectFire.velocity = 0;

			worldServer.unmovables.add(objectFire);
		}

		// give back bomb to user
		if (owner != null) {
			owner.bombCount++;
		}
	}

	@Override
	public void tick(WorldServer worldServer) {
	}

}
