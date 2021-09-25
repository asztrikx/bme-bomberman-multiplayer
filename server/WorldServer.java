package server;

import java.security.SecureRandom;
import java.util.List;

import engine.Collision;
import helper.Config;
import helper.Logger;
import helper.Position;
import world.World;
import world.element.unmovable.Box;
import world.element.unmovable.Exit;
import world.element.unmovable.Unmovable;
import world.element.unmovable.Wall;
import world.movable.Enemy;
import world.movable.Movable;

public class WorldServer extends World {
	private int height, width;
	private Collision collision;

	public WorldServer(Config config, Logger logger) {
		this.collision = new Collision(config, logger);

		if (config.worldHeight % 2 != 1 || config.worldWidth % 2 != 1 || config.worldHeight < 5
				|| config.worldWidth < 5) {
			// TODO java custom exception
			System.out.println("WorldGenerate: World size is malformed");
			System.exit(1);
		}

		// wall generate
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (i == 0 || j == 0 || i == height - 1 || j == width - 1 || (i % 2 == 0 && j % 2 == 0)) {
					Unmovable object = new Wall();
					object.position = new Position(i * config.squaresize, j * config.squaresize);
					objectList.add(object);
				}
			}
		}

		int collisionFreeCountObject = collision.getFreeSpaceCount(this,
				new Position(config.squaresize, config.squaresize));

		// box generate randomly
		for (int i = 0; i < (int) (config.boxRatio * collisionFreeCountObject); i++) {
			Unmovable object = new Box();
			object.position = collision.getSpawn(this);
			objectList.add(object);
		}

		// exit
		Unmovable object = new Exit();
		object.position = objectList.get(0).position;
		object.animation.stateDelayTickEnd = 10;
		objectList.add(object);
		exit = object;

		// enemy generate randomly
		for (int i = 0; i < (int) (config.enemyRatio * collisionFreeCountObject); i++) {
			Movable character = new Enemy(config, logger);
			character.position = collision.getSpawn(this);
			character.velocity = config.velocityEnemy;
			// character.KeyMovementRandom();
			characterList.add(character);
		}
	}

	// SpawnGet return a position where there's at least 3 free space reachable
	// without action so player does not die instantly
	public Position getSpawn(Config config, Logger logger) {
		Collision collision = new Collision(config, logger);

		// position find
		Position positionCompressed;
		Position position;
		int collisionCountCharacter;
		int spawnSquareFreeSpace;
		boolean near = false;
		SecureRandom secureRandom = new SecureRandom();
		do {
			// random position in world
			// this could be a bit optimized but it's more error prone
			positionCompressed = new Position(secureRandom.nextInt(config.worldHeight),
					secureRandom.nextInt(config.worldWidth));

			// decompress
			position = new Position(positionCompressed.y * config.squaresize, positionCompressed.x * config.squaresize);

			// collision check
			List<Movable> collisionCharacterS = collision.collisionsGet(characterList, position, null, null);
			collisionCountCharacter = collisionCharacterS.size();

			// distance check
			near = false;
			for (Movable character : characterList) {
				int maxDistance = config.spawnSquareDistanceFromOthers * config.squaresize;
				if (Math.abs(position.y - character.position.y) < maxDistance
						&& Math.abs(position.x - character.position.x) < maxDistance) {
					near = true;
					break;
				}
			}

			// position valid
			spawnSquareFreeSpace = collision.getFreeSpaceCount(this, position);
		} while (collisionCountCharacter != 0 || spawnSquareFreeSpace < config.spawnSquareFreeSpace || near);

		return position;
	}
}
