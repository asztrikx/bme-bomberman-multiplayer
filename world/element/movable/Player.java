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
	public boolean shouldDestroy(final long tickCount) {
		return owner.state == User.State.Dead;
	}

	@Override
	// checks if colliding with Enemy and kills them if so
	public void nextState(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		super.nextState(worldServer, nextWorldServer, tickCount);

		final List<Movable> collisionMovableS = Collision.getCollisions(worldServer.movables, position, this,
				(final WorldElement worldElementRelative, final Movable that) -> {
					return that instanceof Enemy;
				});
		// death
		if (collisionMovableS.size() != 0) {
			owner.state = User.State.Dead;
		}
	}
}
