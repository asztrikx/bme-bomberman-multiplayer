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

public class Movable extends WorldElement {
	public CharacterType type;
	public int velocity = 0;
	public int bombCount = 0;
	public UserServer owner;
	public Animation animation = new Animation(0, 0, 10);
	public boolean[] keys = new boolean[Key.KeyType.KeyLength];

	Config config;
	Logger logger;
	Collision collision;

	public Movable(Config config, Logger logger) {
		this.logger = logger;
		this.config = config;
		this.collision = new Collision(config, logger);
	}

	public enum CharacterType {
		CharacterTypeUser, CharacterTypeEnemy, CharacterTypeYou,
	}

	// KeyMovement moves character based on it's pressed keys
	void KeyMovement(Movable character, WorldServer worldServer) {
		Position positionNew = character.position;
		if (character.keys[Key.KeyType.KeyUp]) {
			positionNew.y -= character.velocity;
		}
		if (character.keys[Key.KeyType.KeyLeft]) {
			positionNew.x -= character.velocity;
		}
		if (character.keys[Key.KeyType.KeyDown]) {
			positionNew.y += character.velocity;
		}
		if (character.keys[Key.KeyType.KeyRight]) {
			positionNew.x += character.velocity;
		}

		// collision
		positionNew = collision.CollisionLinePositionGet(worldServer, character.position, positionNew, character,
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
		if (character.type == Movable.CharacterType.CharacterTypeEnemy && character.position.equals(positionNew)) {
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				character.keys[i] = false;
			}

			character.keys[secureRandom.nextInt(Key.KeyType.KeyLength)] = true;
		}
		character.position = positionNew;

		// moved out from a bomb with !bombOut
		// in one move it is not possible that it moved out from bomb then moved back
		// again
		for (Unmovable object : worldServer.objectList) {
			if (object.type == Unmovable.ObjectType.ObjectTypeBomb && object.owner == character && !object.bombOut
					&& !collision.doCollide(character.position, object.position)) {
				object.bombOut = true;
			}
		}
	}

	// KeyBombPlace places a bomb to the nearest square in the grid relative to the
	// character
	void KeyBombPlace(Movable character, WorldServer worldServer, long tickCount) {
		// bomb available
		if (character.bombCount == 0) {
			return;
		}

		Position positionNew = character.position;

		// position
		positionNew.y -= positionNew.y % config.squaresize;
		positionNew.x -= positionNew.x % config.squaresize;
		if (character.position.y % config.squaresize > config.squaresize / 2) {
			positionNew.y += config.squaresize;
		}
		if (character.position.x % config.squaresize > config.squaresize / 2) {
			positionNew.x += config.squaresize;
		}

		// collision
		List<Unmovable> collisionObjectS = collision.collisionsGet(worldServer.objectList, positionNew, null, null);
		List<Movable> collisionCharacterS = collision.collisionsGet(worldServer.characterList, positionNew, character,
				null);

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
		object.owner = character;
		object.animation.stateDelayTickEnd = 15;
		worldServer.objectList.add(object);

		// bomb decrease
		character.bombCount--;
	}

	// KeyMovementRandom sets randomly one key to be active
	void KeyMovementRandom(Movable character) {
		SecureRandom secureRandom = new SecureRandom();

		for (int i = 0; i < Key.KeyType.KeyLength; i++) {
			character.keys[i] = false;
		}
		character.keys[secureRandom.nextInt(Key.KeyType.KeyLength)] = true;
	}

}
