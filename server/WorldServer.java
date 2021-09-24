package server;

import engine.Collision;
import helper.Config;
import helper.Logger;
import helper.Position;
import world.World;
import world.element.Movable;
import world.element.Unmovable;

public class WorldServer extends World {
	private Config config;
	private int height, width;
	private Collision collision;
	private Logger logger;

	public WorldServer(Config config, Logger logger) {
		this.config = config;
		this.logger = logger;
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
					Unmovable object = new Unmovable();
					object.position = new Position(i * config.squaresize, j * config.squaresize);
					object.type = Unmovable.ObjectType.ObjectTypeWall;
					objectList.add(object);
				}
			}
		}

		int collisionFreeCountObject = collision.CollisionFreeCountObjectGet(this,
				new Position(config.squaresize, config.squaresize));

		// box generate randomly
		for (int i = 0; i < (int) (config.boxRatio * collisionFreeCountObject); i++) {
			Unmovable object = new Unmovable();
			object.position = SpawnGet(this, 1);
			object.type = Unmovable.ObjectType.ObjectTypeBox;
			objectList.add(object);
		}

		// exit
		Unmovable object = new Unmovable();
		object.position = objectList.get(0).position;
		object.type = Unmovable.ObjectType.ObjectTypeExit;
		object.animation.stateDelayTickEnd = 10;
		objectList.add(object);
		exit = object;

		// enemy generate randomly
		for (int i = 0; i < (int) (config.enemyRatio * collisionFreeCountObject); i++) {
			Movable character = new Movable(config, logger);
			character.position = SpawnGet(this, 3);
			character.type = Movable.CharacterType.CharacterTypeEnemy;
			character.velocity = config.velocityEnemy;
			character.KeyMovementRandom();
			characterList.add(character);
		}
	}
}
