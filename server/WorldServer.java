package server;

import java.security.SecureRandom;
import java.util.List;

import di.DI;
import engine.Collision;
import helper.Config;
import helper.Logger;
import helper.Position;
import world.World;
import world.element.movable.Enemy;
import world.element.movable.Movable;
import world.element.unmovable.Box;
import world.element.unmovable.Exit;
import world.element.unmovable.Wall;

public class WorldServer extends World {
	private static Config config = (Config) DI.services.get(Config.class);
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	public WorldServer() {
		height = config.worldHeight;
		width = config.worldWidth;

		if (height % 2 != 1 || width % 2 != 1 || height < 5 || width < 5) {
			logger.println("config world dimension malformed");
			throw new Error("config world dimension malformed");
		}

		// wall generate
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (i == 0 || j == 0 || i == height - 1 || j == width - 1 || (i % 2 == 0 && j % 2 == 0)) {
					Wall wall = new Wall();
					wall.position = new Position(i * config.squaresize, j * config.squaresize);
					unmovables.add(wall);
				}
			}
		}

		int collisionFreeCountObject = Collision.getFreeSpaceCount(this,
				new Position(config.squaresize, config.squaresize));

		// box generate randomly
		Position lastBoxPosition = null;
		for (int i = 0; i < (int) (config.boxRatio * collisionFreeCountObject); i++) {
			Box box = new Box();
			box.position = getSpawn(1);
			unmovables.add(box);

			lastBoxPosition = box.position;
		}

		// exit
		Exit exit = new Exit();
		exit.position = lastBoxPosition;
		// unmovables.add(exit);
		this.exit = exit;

		// enemy generate randomly
		for (int i = 0; i < (int) (config.enemyRatio * collisionFreeCountObject); i++) {
			Enemy enemy = new Enemy();
			enemy.position = getSpawn(3);
			enemy.velocity = config.velocityEnemy;
			// character.KeyMovementRandom();
			movables.add(enemy);
		}
	}

	// SpawnGet return a position where there's at least 3 free space reachable
	// without action so player does not die instantly
	public Position getSpawn(int minSpawnSquareFreeSpace) {
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
			positionCompressed = new Position(secureRandom.nextInt(height), secureRandom.nextInt(width));

			// decompress
			position = new Position(positionCompressed.y * config.squaresize, positionCompressed.x * config.squaresize);

			// collision check
			List<Movable> collisionCharacterS = Collision.getCollisions(movables, position, null, null);
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
			spawnSquareFreeSpace = Collision.getFreeSpaceCount(this, position);
		} while (collisionCountCharacter != 0 || spawnSquareFreeSpace < minSpawnSquareFreeSpace || near);

		return position;
	}
}
