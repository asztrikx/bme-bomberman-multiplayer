package engine.gameend;

import server.WorldServer;

public class Never implements Gameend {
	/**
	 * Never lets the game end
	 */
	@Override
	public boolean shouldEnd(final WorldServer worldServer, final long tickCount) {
		return false;
	}
}
