package engine.gameend;

import server.WorldServer;

public class Never implements Gameend {
	@Override
	public boolean shouldEnd(WorldServer worldServer, long tickCount) {
		return false;
	}
}
