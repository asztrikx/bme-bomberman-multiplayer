package test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import di.DI;
import engine.Collision;
import helper.Config;
import helper.Position;
import server.WorldServer;
import world.element.unmovable.Box;

public class CollisionTest {
	private static Config config;

	@BeforeAll
	public static void before() {
		DI.init(Config.defaultConfigFileName);
		config = (Config) DI.get(Config.class);
	}

	@Test
	public void doCollide() {
		Position position1 = new Position(0, 0);

		for (int h = -1; h <= 1; h++) {
			for (int w = -1; w <= 1; w++) {
				Position position2 = new Position(h * config.squaresize, w * config.squaresize);
				boolean doCollide = Collision.doCollide(position1, position2);
				if (h == 0 && w == 0) {
					assertTrue(doCollide);
				} else {
					assertFalse(doCollide);
				}

			}
		}
	}

	/**
	 * Get free square size area from # xxxxxx x x x x x x x x x x# x x x xxxxxx
	 */
	@Test
	public void getFreeSpaceCount() {
		WorldServer worldServer = new WorldServer();
		Box box = new Box();
		box.position = new Position(0, 0);
		worldServer.unmovables.add(box);
		assertEquals(0, Collision.getFreeSpaceCount(worldServer, new Position(0, 0)));

		worldServer = new WorldServer();
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 7; j++) {
				if (i == 0 || j == 0 || i == 6 - 1 || j == 7 - 1) {
					box = new Box();
					box.position = new Position(i * config.squaresize, j * config.squaresize);
					worldServer.unmovables.add(box);
				}
			}
		}
		List<Position> positions = new ArrayList<>();
		positions.add(new Position(2, 3));
		positions.add(new Position(3, 3));
		positions.add(new Position(4, 2));

		for (Position position : positions) {
			box = new Box();
			position.y = position.y * config.squaresize;
			position.x = position.x * config.squaresize;
			box.position = position;
			worldServer.unmovables.add(box);
		}

		assertEquals(17,
				Collision.getFreeSpaceCount(worldServer, new Position(4 * config.squaresize, 3 * config.squaresize)));
	}
}
