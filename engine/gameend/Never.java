package engine.gameend;

import server.WorldServer;

public class Never implements Gameend {
	@Override
	public boolean shouldEnd(final WorldServer worldServer, final long tickCount) {
		return false;
	}
}
