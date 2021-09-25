package world.element;

import java.security.SecureRandom;
import java.util.List;

import engine.Collision;
import helper.Config;
import helper.Key;
import helper.Logger;
import helper.Position;
import server.UserServer;
import server.WorldServer;

public abstract class Movable extends WorldElement {
	public CharacterType type;
	public int velocity = 0;
	public int bombCount = 0;
	public UserServer owner;
	public boolean[] keys = new boolean[Key.KeyType.KeyLength];

	Config config;
	Logger logger;
	Collision collision;

	public Movable(Config config, Logger logger) {
		this.logger = logger;
		this.config = config;
		this.collision = new Collision(config, logger);
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
		positionNew = collision.CollisionLinePositionGet(worldServer, position, positionNew, this,
				(Movable characterRelative, Unmovable object) -> {
					return object.type == Unmovable.ObjectType.ObjectTypeWall
							|| object.type == Unmovable.ObjectType.ObjectTypeBox
							|| (object.type == Unmovable.ObjectType.ObjectTypeBomb
									&& (object.owner != characterRelative || object.bombOut));
				}, (Movable objectRelative, Movable movable) -> {
					// CharacterTypeUser is solid for CharacterTypeUser
					// CharacterTypeEnemy is not solid for CharacterTypeUser
					// vice versa with CharacterTypeEnemy
					// so only same type character is solid
					return movable.type == objectRelative.type;
				});

		// enemy new one way direction
		SecureRandom secureRandom = new SecureRandom();
		if (type == Movable.CharacterType.CharacterTypeEnemy && position.equals(positionNew)) {
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				keys[i] = false;
			}

			keys[secureRandom.nextInt(Key.KeyType.KeyLength)] = true;
		}
		position = positionNew;

		// moved out from a bomb with !bombOut
		// in one move it is not possible that it moved out from bomb then moved back
		// again
		for (Unmovable object : worldServer.objectList) {
			if (object.type == Unmovable.ObjectType.ObjectTypeBomb && object.owner == this && !object.bombOut
					&& !collision.doCollide(position, object.position)) {
				object.bombOut = true;
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

		Position positionSquare = position.getSquare(config);

		// position
		Position positionNew = position.sub(positionSquare);
		if (positionSquare.y > config.squaresize / 2) {
			positionNew.y += config.squaresize;
		}
		if (positionSquare.x > config.squaresize / 2) {
			positionNew.x += config.squaresize;
		}

		// collision
		List<Unmovable> collisionObjectS = collision.collisionsGet(worldServer.objectList, positionNew, null, null);
		List<Movable> collisionCharacterS = collision.collisionsGet(worldServer.characterList, positionNew, this, null);

		if (collisionCharacterS.size() != 0 || collisionObjectS.size() != 0) {
			return;
		}

		// bomb insert
		Unmovable object = new Unmovable();
		object.created = tickCount;
		object.destroy = tickCount + 2 * config.tickSecond;
		object.position = positionNew;
		object.type = Unmovable.ObjectType.ObjectTypeBomb;
		object.velocity = 0;
		object.bombOut = false;
		object.owner = this;
		object.animation.stateDelayTickEnd = 15;
		worldServer.objectList.add(object);

		// bomb decrease
		bombCount--;
	}

	public void move(WorldServer worldServer, long tickCount) {
		updateKeys();
		applyMovement(worldServer);
		applyBombPlace(worldServer, tickCount);
	}
}
