public class WorldServer extends World {
	Config config;
	int height, width;

	public WorldServer(Config config) {
		this.config = config;

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
					Object object = new Object();
					object.position = new Position(i * config.squaresize, j * config.squaresize);
					object.type = Object.ObjectType.ObjectTypeWall;
					objectList.add(object);
				}
			}
		}

		int collisionFreeCountObject = CollisionFreeCountObjectGet(this,
				new Position(config.squaresize, config.squaresize));

		// box generate randomly
		for (int i = 0; i < (int) (config.boxRatio * collisionFreeCountObject); i++) {
			Object object = new Object();
			object.position = SpawnGet(this, 1);
			object.type = Object.ObjectType.ObjectTypeBox;
			objectList.add(object);
		}

		// exit
		Object object = new Object();
		object.position = objectList.get(0).position;
		object.type = Object.ObjectType.ObjectTypeExit;
		object.animation.stateDelayTickEnd = 10;
		objectList.add(object);
		exit = object;

		// enemy generate randomly
		for (int i = 0; i < (int) (config.enemyRatio * collisionFreeCountObject); i++) {
			Character character = new Character();
			character.position = SpawnGet(this, 3);
			character.type = Character.CharacterType.CharacterTypeEnemy;
			character.velocity = config.velocityEnemy;
			KeyMovementRandom(character);
			characterList.add(character);
		}
	}
}
