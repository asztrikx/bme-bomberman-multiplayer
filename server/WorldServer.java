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
import world.element.unmovable.Wall;
import world.movable.Enemy;
import world.movable.Movable;

public class WorldServer extends World {
	private Collision collision;
	private Config config;
	private Logger logger;

	public WorldServer(Config config, Logger logger) {
		this.collision = new Collision(config, logger);
		this.config = config;
		this.logger = logger;

		if (config.worldHeight % 2 != 1 || config.worldWidth % 2 != 1 || config.worldHeight < 5
				|| config.worldWidth < 5) {
			// TODO java custom exception
			System.out.println("WorldGenerate: World size is malformed");
			System.exit(1);
		}

		// wall generate
		for (int i = 0; i < config.worldHeight; i++) {
			for (int j = 0; j < config.worldWidth; j++) {
				if (i == 0 || j == 0 || i == config.worldHeight - 1 || j == config.worldWidth - 1
						|| (i % 2 == 0 && j % 2 == 0)) {
					Wall wall = new Wall();
					wall.position = new Position(i * config.squaresize, j * config.squaresize);
					unmovables.add(wall);
				}
			}
		}

		int collisionFreeCountObject = collision.getFreeSpaceCount(this,
				new Position(config.squaresize, config.squaresize));

		// box generate randomly
		for (int i = 0; i < (int) (config.boxRatio * collisionFreeCountObject); i++) {
			Box box = new Box();
			box.position = getSpawn(0);
			unmovables.add(box);
		}

		// exit
		Exit exit = new Exit();
		exit.position = unmovables.get(0).position;
		exit.animation.stateDelayTickEnd = 10;
		unmovables.add(exit);
		this.exit = exit;

		// enemy generate randomly
		for (int i = 0; i < (int) (config.enemyRatio * collisionFreeCountObject); i++) {
			Enemy enemy = new Enemy(config, logger);
			enemy.position = getSpawn(0);
			enemy.velocity = config.velocityEnemy;
			// character.KeyMovementRandom();
			movables.add(enemy);
		}
	}

	// SpawnGet return a position where there's at least 3 free space reachable
	// without action so player does not die instantly
	public Position getSpawn(int minSpawnSquareFreeSpace) {
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
			List<Movable> collisionCharacterS = collision.getCollisions(movables, position, null, null);
			collisionCountCharacter = collisionCharacterS.size();

			// distance check
			near = false;
			for (Movable movable : movables) {
				int minDistance = config.spawnSquareDistanceFromOthers * config.squaresize;
				if (Math.abs(position.y - movable.position.y) < minDistance
						&& Math.abs(position.x - movable.position.x) < minDistance) {
					near = true;
					break;
				}
			}

			// position valid
			spawnSquareFreeSpace = collision.getFreeSpaceCount(this, position);
		} while (collisionCountCharacter != 0 || spawnSquareFreeSpace < minSpawnSquareFreeSpace || near);

		return position;
	}
}
