package engine.gameend;

import java.util.ArrayList;
import java.util.List;

import engine.Collision;
import server.WorldServer;
import world.element.movable.Enemy;
import world.element.movable.Movable;
import world.element.movable.Player;

public interface Gameend {
	/**
	 * @formatter:off
	 * Returns whether the current game should end
	 * @param worldServer
	 * @param tickCount
	 * @return
	 * @formatter:on
	 */
	boolean shouldEnd(WorldServer worldServer, long tickCount);

	/**
	 * @formatter:off
	 * Returns players at exit
	 * @param worldServer
	 * @return
	 * @formatter:on
	 */
	public static List<Player> playersAtExit(final WorldServer worldServer) {
		final List<Player> players = new ArrayList<>();
		for (final Movable movable : worldServer.movables) {
			if (movable instanceof Player) {
				players.add((Player) movable);
			}
		}

		// set user state, remove player
		final List<Player> playersAtExit = Collision.getCollisions(players, worldServer.exit.position, null, null);
		return playersAtExit;
	}

	/**
	 * @formatter:off
	 * Returns the number of enemies alive
	 * @param worldServer
	 * @return
	 * @formatter:on
	 */
	public static boolean enemiesAlive(final WorldServer worldServer) {
		return worldServer.movables.stream().filter(movable -> movable instanceof Enemy).count() > 0;
	}
}
