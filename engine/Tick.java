package engine;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;

import Thing;
import helper.AutoClosableLock;
import helper.Config;
import helper.Gamestate;
import helper.Key;
import helper.Position;
import server.UserServer;
import server.WorldServer;

public class Tick extends TimerTask {
	WorldServer worldServer;
	int tickCount = 0;
	Config config;
	Lock lock;

	public Tick(WorldServer worldServer, Integer t, Config config, Lock lock) {
		this.worldServer = worldServer;
		this.lock = lock;
	}

	@Override
	// Tick calculates new frame, notifies users
	public void run() {
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			TickCalculate();
			TickSend();
		}

		tickCount++;
	}

	// TickCalculateDestroyBomb removes bomb and creates fire in its place
	// if object->type != ObjectTypeBomb then nothing happens
	public void TickCalculateDestroyBomb(Thing thing) {
		// fire inserts
		int directionX[] = { 0, 1, -1, 0, 0 };
		int directionY[] = { 0, 0, 0, 1, -1 };
		for (int j = 0; j < 5; j++) {
			Position position = new Position(thing.position.y + directionY[j] * config.squaresize,
					thing.position.x + directionX[j] * config.squaresize);

			List<Thing> collisionObjectS = CollisionObjectSGet(worldServer.objectList, position, thing, null);
			boolean boxExists = collisionObjectS.isEmpty()
					|| collisionObjectS.stream().filter(t -> t.type == Thing.ObjectType.ObjectTypeBox).count() != 0;
			if (!boxExists && collisionObjectS.size() != 0) {
				continue;
			}

			Thing objectFire = new Thing();
			objectFire.bombOut = true;
			objectFire.created = tickCount;
			objectFire.destroy = tickCount + (long) (0.25 * config.tickSecond);
			objectFire.owner = thing.owner;
			objectFire.position = position;
			objectFire.type = Thing.ObjectType.ObjectTypeBombFire;
			objectFire.animation.stateDelayTickEnd = 2;
			objectFire.velocity = 0;

			worldServer.objectList.add(objectFire);
		}

		// bomb remove
		if (thing.owner != null) {
			thing.owner.bombCount++;
		}
	}

	// TickCalculateFireDestroy makes fires destroys all ObjectTypeBox and all
	// Character in collision
	public void TickCalculateFireDestroy() {
		for (Thing object : worldServer.objectList) {
			if (object.type != Thing.ObjectType.ObjectTypeBombFire) {
				continue;
			}

			// object collision
			List<Thing> collisionObjectS = CollisionObjectSGet(worldServer.objectList, object.position, null, null);
			for (Thing collisionObject : collisionObjectS) {
				if (collisionObject.type == Thing.ObjectType.ObjectTypeBox) {
					worldServer.objectList.remove(collisionObject);
				} else if (collisionObject.type == Thing.ObjectType.ObjectTypeBomb) {
					// chain bomb explosion
					// -
					// bombExplode(objectItemCurrent->object);
				}
			}

			// character collision
			List<Character> collisionCharacterS = CollisionCharacterSGet(worldServer.characterList, object.position,
					null, null);
			for (Character collisionCharacter : collisionCharacterS) {x

				// UserServer update
				if (collisionCharacter.owner != null) {
					collisionCharacter.owner.gamestate = Gamestate.GamestateDead;
				}

				// remove
				worldServer.characterList.remove(collisionCharacter);
			}
		}
	}

	// TickCalculateEnemyKillCollisionDetect is a helper function of
	// TickCalculateEnemyKill
	public boolean TickCalculateEnemyKillCollisionDetect(void* this, Character* that){
		return that->type == CharacterTypeEnemy;
	}

	// TickCalculateWin checks if any CharacterTypeUser if in a winning state and
	// removes them if so
	public void TickCalculateWin() {
		List<Character> collisionCharacterS = CollisionCharacterSGet(worldServer.characterList,
				worldServer.exit.position, null, null);
		for (List<Character> character : collisionCharacterS) {
			if (character.type == Character.CharacterType.CharacterTypeUser && worldServer.characterList.size() == 1) {
				// UserServer update
				character.owner.gamestate = Gamestate.GamestateWon;

				// remove
				Character listItem = ListFindItemByPointer(worldServer.characterList, character);
				worldServer.characterList.remove(listItem);
			}
		}
	}

	// TickCalculateEnemyKill checks if any CharacterTypeUser is colliding with
	// CharacterTypeEnemy and kills them if so
	public void TickCalculateEnemyKill() {
		List<Character> deathS = new ArrayList<>();
		for (Character character : worldServer.characterList) {
			if (character.type != Character.CharacterType.CharacterTypeUser) {
				continue;
			}

			List<Character> collisionCharacterS = CollisionCharacterSGet(worldServer.characterList, character.position,
					character, TickCalculateEnemyKillCollisionDetect);

			// death
			if (collisionCharacterS.size() != 0) {
				character.owner.gamestate = Gamestate.GamestateDead;
				deathS.add(character);
			}
		}
		for (Character character : deathS) {
			worldServer.characterList.remove(character);
		}
	}

	// TickCalculateEnemyMovement randomly creates a new random direction for
	// CharacterTypeEnemys
	public void TickCalculateEnemyMovement() {
		for (Character character : worldServer.characterList) {
			if (character.type != Character.CharacterType.CharacterTypeEnemy) {
				continue;
			}

			SecureRandom secureRandom = new SecureRandom();
			if (secureRandom.nextDouble() > config.enemyKeyChangePossibility) {
				continue;
			}

			KeyMovementRandom(character);
		}
	}

	// TickCalculateDestroy removes items where .destroy == tickCount
	// destroy hooks also added here
	public void TickCalculateDestroy() {
		for (Thing listItemCurrent : worldServer.objectList) {
			if (tickCount != listItemCurrent.destroy) {
				continue;
			}

			if (listItemCurrent.type == Thing.ObjectType.ObjectTypeBomb) {
				TickCalculateDestroyBomb(listItemCurrent);
			}

			// TODO java will iterator break?
			worldServer.objectList.remove(listItemCurrent);
		}
	}

	// TickCalculateAnimate calculates next texture state from current
	public void TickCalculateAnimate() {
		// animate
		for (Thing object : worldServer.objectList) {
			// delay
			object.animation.stateDelayTick++;
			if (object.animation.stateDelayTick <= object.animation.stateDelayTickEnd) {
				continue;
			}
			object.animation.stateDelayTick = 0;

			// state next
			object.animation.state++;
			object.animation.state %= TextureSSObject[object.type].length;
		}
		for (Character character : worldServer.characterList) {
			boolean moving = false;
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				if (character.keys[i]) {
					moving = true;
					break;
				}
			}
			if (!moving) {
				character.animation.state = 0;
				character.animation.stateDelayTick = 0;
				continue;
			}

			// delay
			character.animation.stateDelayTick++;
			if (character.animation.stateDelayTick <= character.animation.stateDelayTickEnd) {
				continue;
			}
			character.animation.stateDelayTick = 0;

			// state next
			character.animation.state++;
			character.animation.state %= TextureSSCharacter[character.type].length;
		}
	}

	// TickCalculate calculates next state from current
	public void TickCalculate() {
		// this should be calculated first as these objects should not exists in this
		// tick
		TickCalculateDestroy();

		// must be before character movement as that fixes bumping into wall
		TickCalculateEnemyMovement();

		// character movement
		// this should be calculated before TickCalculateFireDestroy() otherwise player
		// would be in fire for 1 tick
		// if 2 character is racing for the same spot the first in list wins
		for (Character character : worldServer.characterList) {
			if (character.keys[Key.KeyType.KeyBomb]) {
				KeyBombPlace(character, worldServer, tickCount);
			}
			KeyMovement(character, worldServer);
		}

		// should be before any destroy
		TickCalculateWin();

		TickCalculateFireDestroy();

		TickCalculateEnemyKill();

		TickCalculateAnimate();
	}

	// TickSend sends new world to connected clients
	public void TickSend() {
		for (UserServer userServer : userManager.getList()) {
			// remove exit if not seeable
			List<Object> collisionObjectS = CollisionObjectSGet(worldServer.objectList, worldServer.exit.position,
					worldServer.exit, null);
			Object exit = worldServer.exit;
			if (collisionObjectS.size() != 0) {
				// NetworkSendClient will remove it from list
				worldServer.exit = null;
			}

			// alter user character to be identifiable
			Character character = CharacterFind(userServer);
			if (character != null) {
				character.type = Character.CharacterType.CharacterTypeYou;
			}

			// send
			NetworkSendClient(worldServer, userServer);

			// remove character alter
			if (character != null) {
				character.type = Character.CharacterType.CharacterTypeUser;
			}

			// remove exit remove
			worldServer.exit = exit;
		}
	}

}
