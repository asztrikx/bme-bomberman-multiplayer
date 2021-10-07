package engine.gameend;

import java.util.ArrayList;
import java.util.List;

import engine.Collision;
import server.WorldServer;
import world.element.movable.Enemy;
import world.element.movable.Movable;
import world.element.movable.Player;

public interface Gameend {
	boolean shouldEnd(WorldServer worldServer, long tickCount);

	// checks if any Player if in a winning state and removes them if so
	public static List<Player> playersAtExit(WorldServer worldServer) {
		List<Player> players = new ArrayList<>();
		for (Movable movable : worldServer.movables) {
			if (movable instanceof Player) {
				players.add((Player) movable);
			}
		}

		// set user state, remove player
		List<Player> playersAtExit = Collision.getCollisions(players, worldServer.exit.position, null, null);
		return playersAtExit;
	}

	public static boolean enemiesAlive(WorldServer worldServer) {
		return worldServer.movables.stream().filter(movable -> movable instanceof Enemy).count() > 0;
	}
}
