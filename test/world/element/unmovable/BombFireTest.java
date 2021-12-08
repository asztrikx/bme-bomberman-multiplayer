package test.world.element.unmovable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import di.DI;
import helper.Config;
import helper.Position;
import server.WorldServer;
import world.element.movable.Player;
import world.element.unmovable.Bomb;
import world.element.unmovable.BombFire;
import world.element.unmovable.Box;
import world.element.unmovable.Unmovable;
import world.element.unmovable.Wall;

public class BombFireTest {
	@BeforeAll
	public static void beforeAll() {
		DI.init(Config.defaultConfigFileName);
	}

	/**
	 * @formatter:off
	 * Test fire destroying other WorldElements
	 * B:Box, M:Bomb, F:Fire, W:Wall, P:Playing, -: nothing
	 * 
	 * 1st state
	 * -MB
	 * BFW
	 * -PW
	 * 
	 * 2nd state
	 * -FB
	 * FFW
	 * -FW
	 * @formatter:on
	 */
	@Test
	public void name() {
		Config config = (Config) DI.get(Config.class);
		WorldServer worldServer = new WorldServer();

		List<Position> positions = new ArrayList<>();
		positions.add(new Position(1, 0));
		positions.add(new Position(2, 1));
		positions.add(new Position(2, 2));

		for (Position position : positions) {
			Box box = new Box();
			box.position = new Position(position.y * config.squaresize, position.x * config.squaresize);
			worldServer.unmovables.add(box);
		}

		positions.clear();
		positions.add(new Position(0, 2));
		positions.add(new Position(1, 2));
		for (Position position : positions) {
			Wall wall = new Wall();
			wall.position = new Position(position.y * config.squaresize, position.x * config.squaresize);
			worldServer.unmovables.add(wall);
		}

		Player player = new Player();
		player.position = new Position(1 * config.squaresize, 1 * config.squaresize);
		worldServer.movables.add(player);

		Bomb bomb = new Bomb();
		bomb.position = new Position(1 * config.squaresize, 1 * config.squaresize);
		worldServer.unmovables.add(bomb);

		bomb.destroy(worldServer, worldServer, 1);
		worldServer.unmovables.remove(bomb);

		WorldServer nextWorldServer = new WorldServer();
		nextWorldServer.movables = new LinkedList<>(worldServer.movables);
		nextWorldServer.unmovables = new LinkedList<>(worldServer.unmovables);

		for (Unmovable unmovable : worldServer.unmovables) {
			if (unmovable instanceof BombFire) {
				BombFire bombFire = (BombFire) unmovable;
				bombFire.nextState(worldServer, nextWorldServer, 2);
			}
		}

		assertEquals(3 + (5 - 1), nextWorldServer.unmovables.size() + nextWorldServer.movables.size());
	}
}
