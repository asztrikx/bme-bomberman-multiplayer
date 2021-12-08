package test.engine.gameend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import di.DI;
import engine.gameend.FirstExit;
import helper.Config;
import helper.Position;
import server.WorldServer;
import user.User;
import world.element.movable.Enemy;
import world.element.movable.Player;
import world.element.unmovable.Exit;

public class FirstExitTest {
	@BeforeAll
	public static void beforeAll() {
		DI.init(Config.defaultConfigFileName);
	}

	@Test
	public void testWin() {
		WorldServer worldServer = new WorldServer();

		Player player = new Player();
		player.position = new Position(0, 0);
		player.owner = new User();
		Exit exit = new Exit();
		exit.position = new Position(100, 100);

		worldServer.movables.add(player);
		worldServer.exit = exit;

		FirstExit firstExit = new FirstExit();
		assertFalse(firstExit.shouldEnd(worldServer, 0));

		exit.position = new Position(0, 0);
		assertTrue(firstExit.shouldEnd(worldServer, 0));

		worldServer.movables.add(new Enemy());
		assertFalse(firstExit.shouldEnd(worldServer, 0));
	}
}
