package world.movable;

import java.util.List;

import engine.Collision;
import server.WorldServer;
import user.User;
import world.element.Animation;
import world.element.WorldElement;

public class Player extends Movable {
	public boolean you = false;

	private Collision collision = Collision.Injected;

	public Player() {
		super(new Animation(10, Player.class.getSimpleName()));
	}

	@Override
	public void destroy(WorldServer worldServer) {
		return;
	}

	@Override
	// checks if colliding with Enemy and kills them if so
	public void tick(WorldServer worldServer) {
		List<Movable> collisionMovableS = collision.getCollisions(worldServer.movables, position, this,
				(WorldElement worldElementRelative, Movable that) -> {
					return that instanceof Enemy;
				});
		// death
		if (collisionMovableS.size() != 0) {
			owner.state = User.State.Dead;
		}
	}
}
