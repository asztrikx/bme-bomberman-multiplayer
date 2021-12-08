package test.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import di.DI;
import helper.Config;
import server.WorldServer;
import world.element.unmovable.Wall;

public class WorldServerTest {
	@BeforeAll
	public static void beforeAll() {
		DI.init(Config.defaultConfigFileName);
	}

	@Test
	public void name() {
		WorldServer worldServer = new WorldServer();
		worldServer.width = 11;
		worldServer.height = 23;
		worldServer.generate();
		long boxCount = worldServer.unmovables.stream().filter(unmovable -> unmovable instanceof Wall).count();
		long ouside = worldServer.width * 2 + (worldServer.height - 2) * 2;
		long inside = (worldServer.width - 3) / 2 * (worldServer.height - 3) / 2;
		assertEquals(ouside + inside, boxCount);
	}
}
