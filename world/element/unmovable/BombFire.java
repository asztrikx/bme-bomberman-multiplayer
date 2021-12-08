package world.element.unmovable;

import java.util.ArrayList;
import java.util.List;

import engine.Collision;
import server.WorldServer;
import user.User;
import world.element.Animation;
import world.element.WorldElement;
import world.element.movable.Movable;

public class BombFire extends Unmovable {
	public BombFire() {
		super(new Animation(2, "resource/unmovable/bombFire"));
	}

	/**
	 * Destroys all colliding Box and Player
	 */
	@Override
	public void nextState(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		final List<WorldElement> worldElements = new ArrayList<>();
		worldElements.addAll(worldServer.unmovables);
		worldElements.addAll(worldServer.movables);

		final List<WorldElement> collisionWorldElements = Collision.getCollisions(worldElements, position, null, null);
		for (final WorldElement collisionWorldElement : collisionWorldElements) {
			if (collisionWorldElement instanceof Box) {
				nextWorldServer.unmovables.remove(collisionWorldElement);
			} else if (collisionWorldElement instanceof Bomb) {
				// chain bomb explosion
				// -
			} else if (collisionWorldElement instanceof Movable) {
				final Movable movable = (Movable) collisionWorldElement;

				// UserServer update
				if (movable.owner != null) {
					movable.owner.state = User.State.Dead;
				}

				nextWorldServer.movables.remove(movable);
			}
		}
	}
}
