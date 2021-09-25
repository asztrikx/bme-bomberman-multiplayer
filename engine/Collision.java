package engine;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import helper.Config;
import helper.Logger;
import helper.Position;
import server.WorldServer;
import world.element.WorldElement;
import world.element.unmovable.Unmovable;
import world.movable.Movable;

public class Collision {
	Config config;
	Logger logger;

	public Collision(Config config, Logger logger) {
		this.config = config;
		this.logger = logger;
	}

	// Collision tells whether there's a collision between objects at positions
	public boolean doCollide(Position position1, Position position2) {
		if (Math.abs(position1.x - position2.x) >= config.squaresize) {
			return false;
		}
		if (Math.abs(position1.y - position2.y) >= config.squaresize) {
			return false;
		}
		return true;
	}

	// CollisionObjectSGet returns a List with Objects colliding with this
	// collisionDecideObjectFunction decides for each object whether it should be
	// taking into account
	// if collisionDecideObjectFunction is NULL then it's treated as always true
	public <E extends WorldElement, E2 extends WorldElement> List<E> collisionsGet(List<E> worldElements,
			Position position, E2 worldElementRelative, BiFunction<E2, E, Boolean> collisionDecide) {
		List<E> listCollision = new ArrayList<>();

		for (E worldElement : worldElements) {
			if (worldElement == worldElementRelative) {
				continue;
			}

			if (!doCollide(position, worldElement.position)) {
				continue;
			}

			if (collisionDecide != null && !collisionDecide.apply(worldElementRelative, worldElement)) {
				continue;
			}

			listCollision.add(worldElement);
		}

		return listCollision;
	}

	// CollisionLinePositionGet calculates position taking collision into account in
	// discrete line (from, to)
	// from must not be equal to to
	// we can be NULL
	// if collisionDecideObjectFunction is NULL then it's treated as always true
	// if collisionDecideCharacterFunction is NULL then it's treated as always true
	public <E extends WorldElement> Position CollisionLinePositionGet(WorldServer worldServer, Position from,
			Position to, E we, BiFunction<E, Unmovable, Boolean> collisionDecideObjectFunction,
			BiFunction<E, Movable, Boolean> collisionDecideCharacterFunction) {
		// position difference in abs is always same for y and x coordinate if none of
		// them is zero
		int step = Math.abs(to.y - from.y);
		if (step == 0) {
			step = Math.abs(to.x - from.x);
		}
		Position current = from;

		// stays in place
		if (step == 0) {
			return from;
		}

		// walk by discretely with unit vector as there are scenarios where
		// the collision would misbehave if we would only check the arrival position
		// eg: too fast speed would make it able to cross walls
		// eg: squaresize pixel wide diagonal is crossable this way
		Position unit = new Position(0, 0);
		if (to.y - from.y != 0) {
			unit.y = (to.y - from.y) / Math.abs(to.y - from.y);
		}
		if (to.x - from.x != 0) {
			unit.x = (to.x - from.x) / Math.abs(to.x - from.x);
		}
		for (int i = 0; i < step; i++) {
			// step y
			current.y += unit.y;

			List<Unmovable> collisionObjectS = collisionsGet(worldServer.objectList, current, we,
					collisionDecideObjectFunction);
			List<Movable> collisionCharacterS = collisionsGet(worldServer.characterList, current, we,
					collisionDecideCharacterFunction);
			if (collisionObjectS.size() != 0 || collisionCharacterS.size() != 0) {
				current.y -= unit.y;
			}

			// step x
			current.x += unit.x;

			collisionObjectS = collisionsGet(worldServer.objectList, current, we, collisionDecideObjectFunction);
			collisionCharacterS = collisionsGet(worldServer.characterList, current, we,
					collisionDecideCharacterFunction);
			if (collisionObjectS.size() != 0 || collisionCharacterS.size() != 0) {
				current.x -= unit.x;
			}
		}

		return current;
	}

	private boolean[][] collisionFreeCountObjectGetMemory;

	// CollisionFreeCountObjectGetRecursion is a helper function of
	// CollisionFreeCountObjectGet
	private int getFreeSpaceCountRecursion(WorldServer worldServer, Position positionCompress) {
		Position position = new Position(positionCompress.y * config.squaresize,
				positionCompress.x * config.squaresize);

		// in calculation or already calculated
		if (collisionFreeCountObjectGetMemory[positionCompress.y][positionCompress.x]) {
			return 0;
		}

		// because of map border there will be no overindexing
		// mark invalid positions also to save collision recalculation
		collisionFreeCountObjectGetMemory[positionCompress.y][positionCompress.x] = true;

		// position is valid
		List<Unmovable> collisionObjectS = collisionsGet(worldServer.objectList, position, null, null);
		int collisionCount = collisionObjectS.size();

		if (collisionCount != 0) {
			return 0;
		}

		// neighbour positions
		int collisionFreeCountObject = 1; // current position
		int directionY[] = { 1, -1, 0, 0 };
		int directionX[] = { 0, 0, 1, -1 };
		for (int i = 0; i < 4; i++) {
			Position positionCompressNew = new Position(positionCompress.y + directionY[i],
					positionCompress.x + directionX[i]);
			collisionFreeCountObject += getFreeSpaceCountRecursion(worldServer, positionCompressNew);
		}

		return collisionFreeCountObject;
	}

	// CollisionFreeCountObjectGet returns how many square sized object-free area is
	// reachable from (position - position % squaresize)
	public int getFreeSpaceCount(WorldServer worldServer, Position position) {
		// memory alloc
		collisionFreeCountObjectGetMemory = new boolean[config.worldHeight][config.windowWidth];

		// recursion
		Position positionCompress = new Position(position.y / config.squaresize, position.x / config.squaresize);
		int count = getFreeSpaceCountRecursion(worldServer, positionCompress);

		return count;
	}

	// SpawnGet return a position where there's at least 3 free space reachable
	// without action so player does not die instantly
	public Position getSpawn(WorldServer worldServer) {
		// random max check
		// as it is already in int there is no need to check

		// position find
		Position positionCompressed = new Position(0, 0);
		Position position = new Position(0, 0);
		int collisionCountCharacter;
		int collisionFreeCountObject;
		boolean near = false;
		SecureRandom secureRandom = new SecureRandom();
		do {
			// random position in world
			// this could be a bit optimized but it's more error prone
			positionCompressed.y = secureRandom.nextInt(config.worldHeight);
			positionCompressed.x = secureRandom.nextInt(config.worldWidth);

			// decompress
			position.y = positionCompressed.y * config.squaresize;
			position.x = positionCompressed.x * config.squaresize;

			// collision check
			List<Movable> collisionCharacterS = collisionsGet(worldServer.characterList, position, null, null);
			collisionCountCharacter = collisionCharacterS.size();

			// distance check
			near = false;
			for (Movable movable : worldServer.characterList) {
				if (Math.abs(position.y - movable.position.y) < 3 * config.squaresize
						&& Math.abs(position.x - movable.position.x) < 3 * config.squaresize) {
					near = true;
					break;
				}
			}

			// position valid
			collisionFreeCountObject = getFreeSpaceCount(worldServer, position);
		} while (collisionCountCharacter != 0 || collisionFreeCountObject < config.spawnSquareFreeSpace || near);

		return position;
	}

}
