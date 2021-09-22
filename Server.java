import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class Server {
	UserManager<UserServer> userManager = new UserManager<>();
	Lock mutex = new ReentrantLock();
	Config config;
	Logger logger;
	WorldServer worldServer;
	long tickCount = 0;
	long tickId = 0;
	boolean stopped = true;

	public Server(Logger logger, Config config) {
		this.logger = logger;
		this.config = config;
		worldServer = new WorldServer(config); // not critical section
	}

	public void Listen() {
		stopped = false;

		// key press
		SDL_AddEventWatch(EventKey, NULL);

		// network start
		NetworkServerStart();

		// tick start: world calc, connected user update
		tickId = SDL_AddTimer(tickRate, Tick, NULL);
		if (tickId == 0) {
			SDL_Log("SDL_AddTimer: %s", SDL_GetError());
			exit(1);
		}
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
	public bool TickCalculateEnemyKillCollisionDetect(void* this, Character* that){
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

			if (rand() % RAND_MAX + 1 > RAND_MAX * enemyKeyChangePossibility) {
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

	// Tick calculates new frame, notifies users
	Uint32 Tick(Uint32 interval, void *param){
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			TickCalculate();
	
			TickSend();	
		}

		tickCount++;
		return interval;
	}

	// EventKey handles WorldServer saving
	static int EventKey(void* data, SDL_Event* sdl_event){
		if(
			sdl_event->type != SDL_KEYDOWN ||
			sdl_event->key.keysym.sym != SDLK_q
		){
			return 0;
		}

		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			Save();
		}

		return 0;
	}

	// ServerReceive gets updates from users
	// userServerUnsafe is not used after return
	void ServerReceive(UserServer userServerUnsafe) {
		if (stopped) {
			return;
		}

		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			// auth validate
			// auth's length validation
			// -
			int length = userServerUnsafe.auth.length();
			if (length != 26) {
				return;
			}
			// auth's length validation
			// -
			UserServer userServer = userManager.findByAuth(userServerUnsafe.auth);
			if (userServer == null) {
				return;
			}
			Character character = CharacterFind(userServer);

			// alive
			if (character == null) {
				return;
			}

			// name change
			// TODO java max 15 length
			if (!userServer.name.equals(userServerUnsafe.name)) {
				userServer.name = userServerUnsafe.name;
			}

			// keyS's length validation
			// -

			// keyS copy
			for (int i = 0; i < KeyLength; i++) {
				character.keyS[i] = userServerUnsafe.keyS[i];
			}
		}
	}

	// ServerStop clears server module
	void ServerStop() {
		if (!SDL_RemoveTimer(tickId)) {
			SDL_Log("ServerStop: SDL_RemoveTimer: %s", SDL_GetError());
			exit(1);
		}

		// wait timers to finish
		if (SDL_LockMutex(mutex) != 0) {
			SDL_Log("ServerStop: SDL_LockMutex: %s", SDL_GetError());
			exit(1);
		}

		// need to be called before NetworkServerStop as incoming message may already be
		// coming which
		// could get stuck if SDL_DestroyMutex happens before SDL_LockMutex
		stopped = true;

		NetworkServerStop();

		SDL_DestroyMutex(mutex);
	}

	// ServerConnect register new connection user, returns it with auth
	// userServerUnsafe is not used after return
	void ServerConnect(UserServer userServerUnsafe){
		if (SDL_LockMutex(mutex) != 0){
			SDL_Log("ServerConnect: SDL_LockMutex: %s", SDL_GetError());
			exit(1);
		}

		//userServer copy
		UserServer userServer = new UserServer();
		// TODO java max 15 length
		userServer.name = userServerUnsafe.name;
		userServer.gamestate = Gamestate.GamestateRunning;

		//userServer insert
		userManager.add(userServer);

		//id generate
		while (true){
			Auth auth = new Auth();
			
			//id exists
			if(userManager.findByAuth(auth) == null){
				userServer.auth = auth;
				break;
			}
		}

		//spawn
		Position position = SpawnGet(worldServer, 3);

		//character insert
		Character character = new Character();
		character.bombCount = 1;
		character.owner = userServer;
		character.position = new Position(
			position.y,
			position.x,
		);
		character.type = Character.CharacterType.CharacterTypeUser;
		character.velocity = velocity;
		worldServer.characterList = character;

		//reply
		userServerUnsafe.auth = userServer.auth;

		if(SDL_UnlockMutex(mutex) < 0){
			SDL_Log("ServerConnect: mutex unlock: %s", SDL_GetError());
			exit(1);
		}
	}

}
