package world.movable;

import java.util.List;

import engine.Collision;
import helper.Config;
import helper.Key;
import helper.Position;
import server.UserServer;
import server.WorldServer;
import world.element.Animation;
import world.element.WorldElement;
import world.element.unmovable.Bomb;
import world.element.unmovable.Box;
import world.element.unmovable.Unmovable;
import world.element.unmovable.Wall;

public abstract class Movable extends WorldElement {
	public int velocity = 0;
	public int bombCount = 0;
	public UserServer owner;
	public boolean[] keys = new boolean[Key.KeyType.KeyLength];

	private Config config = Config.Injected;
	private Collision collision = Collision.Injected;

	public Movable(Animation animation) {
		super(animation);
	}

	// TODO for all enum: static?
	public static enum CharacterType {
		CharacterTypeUser(0), CharacterTypeEnemy(1), CharacterTypeYou(2);

		// https://stackoverflow.com/a/8157790/4404911
		private final int value;

		private CharacterType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	// KeyMovement moves character based on it's pressed keys
	public void applyMovement(WorldServer worldServer) {
		Position positionNew = position;
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
		positionNew = collision.getValidPositionOnLine(worldServer, position, positionNew, this,
				(Movable characterRelative, Unmovable object) -> {
					return object instanceof Wall || object instanceof Box
							|| (object instanceof Bomb && (object.owner != characterRelative || object.bombOut));
				}, (Movable objectRelative, Movable movable) -> {
					// CharacterTypeUser is solid for CharacterTypeUser
					// CharacterTypeEnemy is not solid for CharacterTypeUser
					// vice versa with CharacterTypeEnemy
					// so only same type character is solid
					return !(movable instanceof Player && objectRelative instanceof Player
							|| movable instanceof Enemy && objectRelative instanceof Enemy);
				});

		// enemy new one way direction
		if (this instanceof Enemy && position.equals(positionNew)) {
			Enemy enemy = (Enemy) this;
			enemy.randomKeys();
		}
		position = positionNew;

		// moved out from a bomb with !bombOut
		// in one move it is not possible that it moved out from bomb then moved back
		// again
		for (Unmovable unmovable : worldServer.unmovables) {
			// TODO only works for 1 bomb
			if (unmovable instanceof Bomb && unmovable.owner == this && !unmovable.bombOut
					&& !collision.doCollide(position, unmovable.position)) {
				unmovable.bombOut = true;
			}
		}
	}

	// KeyBombPlace places a bomb to the nearest square in the grid relative to the
	// character
	public void applyBombPlace(WorldServer worldServer, long tickCount) {
		// bomb available
		if (bombCount == 0) {
			return;
		}

		// key pressed
		if (!keys[Key.KeyType.KeyBomb.getValue()]) {
			return;
		}

		Position positionSquare = position.getSquare();

		// position
		Position positionNew = position.sub(positionSquare);
		if (positionSquare.y > config.squaresize / 2) {
			positionNew.y += config.squaresize;
		}
		if (positionSquare.x > config.squaresize / 2) {
			positionNew.x += config.squaresize;
		}

		// collision
		List<Unmovable> collisionObjectS = collision.getCollisions(worldServer.unmovables, positionNew, null, null);
		List<Movable> collisionCharacterS = collision.getCollisions(worldServer.movables, positionNew, this, null);

		if (collisionCharacterS.size() != 0 || collisionObjectS.size() != 0) {
			return;
		}

		// bomb insert
		Unmovable object = new Bomb();
		object.createdTick = tickCount;
		object.destroyTick = tickCount + 2 * config.tickSecond;
		object.position = positionNew;
		object.velocity = 0;
		object.bombOut = false;
		object.owner = this;
		object.animation.stateDelayTickEnd = 15;
		worldServer.unmovables.add(object);

		// bomb decrease
		bombCount--;
	}

	public void move(WorldServer worldServer, long tickCount) {
		applyMovement(worldServer);
		applyBombPlace(worldServer, tickCount);
	}
}
