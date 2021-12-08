package world.element.movable;

import java.util.List;

import di.DI;
import engine.Collision;
import helper.Config;
import helper.Key;
import helper.Position;
import server.WorldServer;
import user.User;
import world.element.Animation;
import world.element.WorldElement;
import world.element.unmovable.Bomb;
import world.element.unmovable.Box;
import world.element.unmovable.Unmovable;
import world.element.unmovable.Wall;

public abstract class Movable extends WorldElement {
	private static Config config = (Config) DI.get(Config.class);

	public int velocity = 0;
	public int bombCount = 0;
	public User owner;
	public boolean[] keys = new boolean[Key.KeyType.KeyLength];

	public Movable(final Animation animation) {
		super(animation);
	}

	/**
	 * @formatter:off
	 * Moves based on keys array
	 * Takes collision into account
	 * Handles moving out of bomb
	 * @param worldServer
	 * @param nextWorldServer
	 * @param tickCount
	 * @formatter:on
	 */
	public void applyMovement(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		Position positionNew = new Position(position.y, position.x);
		if (keys[Key.KeyType.KeyUp.getValue()]) {
			positionNew.y -= velocity;
		}
		if (keys[Key.KeyType.KeyLeft.getValue()]) {
			positionNew.x -= velocity;
		}
		if (keys[Key.KeyType.KeyDown.getValue()]) {
			positionNew.y += velocity;
		}
		if (keys[Key.KeyType.KeyRight.getValue()]) {
			positionNew.x += velocity;
		}

		// collision
		position = Collision.getValidPositionOnLine(worldServer, position, positionNew, this,
				(movableRelative, unmovable) -> {
					return unmovable instanceof Wall || unmovable instanceof Box || (unmovable instanceof Bomb
							&& (unmovable.owner != movableRelative || unmovable.movedOutOfBomb));
				}, (movableRelative, movable) -> {
					// Player is solid for Player
					// Enemy is not solid for Player
					// vice versa with Enemy
					// so only same type is solid
					return movable instanceof Player && movableRelative instanceof Player
							|| movable instanceof Enemy && movableRelative instanceof Enemy;
				});

		// moved out from a bomb with !bombOut
		// in one move it is not possible that it moved out from bomb then moved back
		// again
		for (final Unmovable unmovable : worldServer.unmovables) {
			if (unmovable instanceof Bomb && unmovable.owner == this && !unmovable.movedOutOfBomb
					&& !Collision.doCollide(position, unmovable.position)) {
				unmovable.movedOutOfBomb = true;
			}
		}
	}

	/**
	 * @formatter:off
	 * Handles bomb place (in nearest block size) if no collision happens
	 * @param worldServer
	 * @param nextWorldServer
	 * @param tickCount
	 * @formatter:on
	 */
	public void applyBombPlace(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		// bomb available
		if (bombCount == 0) {
			return;
		}

		// key pressed
		if (!keys[Key.KeyType.KeyBomb.getValue()]) {
			return;
		}

		final Position positionSquare = position.getSquare();

		// position
		final Position positionNew = position.sub(positionSquare);
		if (positionSquare.y > config.squaresize / 2) {
			positionNew.y += config.squaresize;
		}
		if (positionSquare.x > config.squaresize / 2) {
			positionNew.x += config.squaresize;
		}

		// collision
		final List<Unmovable> collisionUnmovables = Collision.getCollisions(worldServer.unmovables, positionNew, null,
				null);
		final List<Movable> collisionMovables = Collision.getCollisions(worldServer.movables, positionNew, this, null);

		if (collisionMovables.size() != 0 || collisionUnmovables.size() != 0) {
			return;
		}

		// bomb insert
		final Unmovable bomb = new Bomb();
		bomb.createdTick = tickCount;
		bomb.destroyTick = tickCount + 2 * config.tickSecond;
		bomb.position = positionNew;
		bomb.velocity = 0;
		bomb.movedOutOfBomb = false;
		bomb.owner = this;
		bomb.animation.stateDelayTickEnd = 15;
		nextWorldServer.unmovables.add(bomb);

		// bomb decrease
		bombCount--;
	}

	/**
	 * Moves and handles bomb place
	 */
	@Override
	public void nextState(final WorldServer worldServer, final WorldServer nextWorldServer, final long tickCount) {
		applyMovement(worldServer, nextWorldServer, tickCount);
		applyBombPlace(worldServer, nextWorldServer, tickCount);
	}
}
