package world.element.movable;

import java.util.List;

import engine.Collision;
import server.WorldServer;
import user.User;
import world.element.Animation;
import world.element.WorldElement;

public class Player extends Movable {
	// TODO eh
	public boolean you = false;

	public Player() {
		super(new Animation(10, "resource/movable/player"));
	}

	@Override
	// checks if colliding with Enemy and kills them if so
	public void nextState(WorldServer worldServer, long tickCount) {
		super.nextState(worldServer, tickCount);

		List<Movable> collisionMovableS = Collision.getCollisions(worldServer.movables, position, this,
				(WorldElement worldElementRelative, Movable that) -> {
					return that instanceof Enemy;
				});
		// death
		if (collisionMovableS.size() != 0) {
			owner.state = User.State.Dead;
		}
	}
}