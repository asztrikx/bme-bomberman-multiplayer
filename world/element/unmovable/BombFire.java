package world.element.unmovable;

import java.util.ArrayList;
import java.util.List;

import di.DI;
import engine.Collision;
import server.WorldServer;
import user.User;
import world.element.Animation;
import world.element.WorldElement;
import world.element.movable.Movable;

public class BombFire extends Unmovable {
	private static Collision collision = (Collision) DI.services.get(Collision.class);

	public BombFire() {
		super(new Animation(2, "resource/unmovable/bombFire"));
	}

	@Override
	// destroys all ObjectTypeBox and all Character in collision
	public void nextState(WorldServer worldServer, long tickCount) {
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
