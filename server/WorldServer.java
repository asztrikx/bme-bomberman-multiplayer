package server;

import java.security.SecureRandom;
import java.util.LinkedList;
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
	private static Config config = (Config) DI.get(Config.class);
	private static Logger logger = (Logger) DI.get(Logger.class);

	/**
	 * Randomly generates new map based on config
	 */
	public void generate() {
		movables = new LinkedList<>();
		unmovables = new LinkedList<>();
		height = config.worldHeight;
		width = config.worldWidth;
		exit = null;

		if (height % 2 != 1 || width % 2 != 1 || height < 5 || width < 5) {
			logger.println("config world dimension malformed");
			throw new Error("config world dimension malformed");
		}

		// wall generate
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (i == 0 || j == 0 || i == height - 1 || j == width - 1 || (i % 2 == 0 && j % 2 == 0)) {
					final Wall wall = new Wall();
					wall.position = new Position(i * config.squaresize, j * config.squaresize);
					unmovables.add(wall);
				}
			}
		}

		final int count = Collision.getFreeSpaceCount(this, new Position(config.squaresize, config.squaresize));

		// box generate randomly
		Position lastBoxPosition = null;
		for (int i = 0; i < (int) (config.boxRatio * count); i++) {
			final Box box = new Box();
			box.position = getSpawn(1);
			unmovables.add(box);

			lastBoxPosition = box.position;
		}

		// exit
		final Exit exit = new Exit();
		if (lastBoxPosition == null) {
			exit.position = getSpawn(1);
		} else {
			exit.position = lastBoxPosition;
		}
		unmovables.add(exit);
		this.exit = exit;

		// enemy generate randomly
		for (int i = 0; i < (int) (config.enemyRatio * count); i++) {
			final Enemy enemy = new Enemy();
			enemy.position = getSpawn(3);
			enemy.velocity = config.velocityEnemy;
			// enemy.KeyMovementRandom();
			movables.add(enemy);
		}
	}

	/**
	 * @formatter:off
	 * Return a position where there's at least minSpawnSquareFreeSpace free space available
	 * @param minSpawnSquareFreeSpace
	 * @return
	 * @formatter:on
	 */
	public Position getSpawn(final int minSpawnSquareFreeSpace) {
		// position find
		Position positionCompressed;
		Position position;
		int collisionCountMovable;
		int spawnSquareFreeSpace;
		boolean near = false;
		final SecureRandom secureRandom = new SecureRandom();
		do {
			// random position in world
			// this could be a bit optimized but it's more error prone
			positionCompressed = new Position(secureRandom.nextInt(height), secureRandom.nextInt(width));

			// decompress
			position = new Position(positionCompressed.y * config.squaresize, positionCompressed.x * config.squaresize);

			// collision check
			final List<Movable> collisionMovableS = Collision.getCollisions(movables, position, null, null);
			collisionCountMovable = collisionMovableS.size();

			// distance check
			near = false;
			for (final Movable movable : movables) {
				final int minDistance = config.spawnSquareDistanceFromOthers * config.squaresize;
				if (Math.abs(position.y - movable.position.y) < minDistance
						&& Math.abs(position.x - movable.position.x) < minDistance) {
					near = true;
					break;
				}
			}

			// position valid
			spawnSquareFreeSpace = Collision.getFreeSpaceCount(this, position);
		} while (collisionCountMovable != 0 || spawnSquareFreeSpace < minSpawnSquareFreeSpace || near);

		return position;
	}
}
