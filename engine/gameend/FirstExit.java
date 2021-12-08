package engine.gameend;

import java.util.List;

import server.WorldServer;
import user.User;
import world.element.movable.Player;

/**
 * Win is determined by first player touching exit when all enemies are dead
 */
public class FirstExit implements Gameend {
	@Override
	public boolean shouldEnd(final WorldServer worldServer, final long tickCount) {
		if (Gameend.enemiesAlive(worldServer)) {
			return false;
		}

		final List<Player> playersWinning = Gameend.playersAtExit(worldServer);
		if (playersWinning.size() == 0) {
			return false;
		}

		for (final Player player : playersWinning) {
			player.owner.state = User.State.Won;
		}
		// worldServer.movables.removeAll(playersAtExit);
		return true;
	}
}
