package world.element.unmovable;

import java.util.ArrayList;
import java.util.List;

import engine.Collision;
import server.WorldServer;
import user.User;
import world.element.Animation;
import world.element.WorldElement;
import world.movable.Movable;

public class BombFire extends Unmovable {
	private Collision collision = Collision.Injected;

	public BombFire() {
		super(new Animation(2, BombFire.class.getSimpleName()));
	}

	@Override
	public void destroy(WorldServer worldServer) {
	}

	@Override
	// destroys all ObjectTypeBox and all Character in collision
	public void tick(WorldServer worldServer) {
		List<WorldElement> worldElements = new ArrayList<>();
		worldElements.addAll(worldServer.unmovables);
		worldElements.addAll(worldServer.movables);

		List<WorldElement> collisionWorldElements = collision.getCollisions(worldElements, position, null, null);
		List<WorldElement> deletelist = new ArrayList<>();
		for (WorldElement collisionWorldElement : collisionWorldElements) {
			if (collisionWorldElement instanceof Box) {
				deletelist.add(collisionWorldElement);
			} else if (collisionWorldElement instanceof Bomb) {
				// chain bomb explosion
				// -
				// bombExplode(objectItemCurrent->object);
			} else if (collisionWorldElement instanceof Movable) {
				Movable movable = (Movable) collisionWorldElement;

				// UserServer update
				if (movable.owner != null) {
					movable.owner.state = User.State.Dead;
				}

				deletelist.add(movable);
			}
		}
		worldServer.unmovables.removeAll(deletelist);
		worldServer.movables.removeAll(deletelist);
	}
}
