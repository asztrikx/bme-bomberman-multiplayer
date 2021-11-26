package engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import client.WorldClient;
import di.DI;
import engine.gameend.Gameend;
import helper.Key;
import helper.Logger;
import server.WorldServer;
import user.User;
import user.User.State;
import world.element.movable.Movable;
import world.element.movable.Player;
import world.element.unmovable.Exit;
import world.element.unmovable.Unmovable;

/**
 * Must be called with lock closed
 */
public class Tick {
	private Logger logger = (Logger) DI.get(Logger.class);

	private final WorldServer worldServer;
	public long tickCount = 0;
	public Gameend gameend;

	public Tick(final WorldServer worldServer, final Gameend gameend) {
		this.worldServer = worldServer;
		this.gameend = gameend;
	}

	public void nextStateAnimate() {
		// animate
		for (final Unmovable unmovable : worldServer.unmovables) {
			unmovable.animation.increase();
		}
		for (final Movable movable : worldServer.movables) {
			boolean moving = false;
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				if (movable.keys[i]) {
					moving = true;
					break;
				}
			}
			if (!moving) {
				movable.animation.reset();
				continue;
			}

			movable.animation.increase();
		}
	}

	// calculates next state from current
	public boolean nextState() {
		final WorldServer nextWorldServer = new WorldServer();
		nextWorldServer.movables = new LinkedList<>(worldServer.movables);
		nextWorldServer.unmovables = new LinkedList<>(worldServer.unmovables);

		for (final Unmovable unmovable : worldServer.unmovables) {
			unmovable.nextState(worldServer, nextWorldServer, tickCount);
			if (unmovable.shouldDestroy(tickCount)) {
				unmovable.destroy(worldServer, nextWorldServer, tickCount);
				nextWorldServer.unmovables.remove(unmovable);
			}
		}

		for (final Movable movable : worldServer.movables) {
			movable.nextState(worldServer, nextWorldServer, tickCount);
			if (movable.shouldDestroy(tickCount)) {
				movable.destroy(worldServer, nextWorldServer, tickCount);
				nextWorldServer.movables.remove(movable);
			}
		}

		nextStateAnimate();

		if (gameend.shouldEnd(worldServer, tickCount)) {
			for (final Movable movable : worldServer.movables) {
				if (movable instanceof Player) {
					final Player player = (Player) movable;
					player.owner.state = State.Won;
				}
			}
			logger.println("Match ended");
			return false;
		}

		tickCount++;
		worldServer.movables = nextWorldServer.movables;
		worldServer.unmovables = nextWorldServer.unmovables;

		return true;
	}

	public WorldClient getWorldClient() {
		WorldClient worldClient = new WorldClient();

		// remove exit if behind box
		final List<Unmovable> collisionObjectS = Collision.getCollisions(worldServer.unmovables,
				worldServer.exit.position, worldServer.exit, null);
		if (collisionObjectS.size() == 0) {
			worldClient.exit = worldServer.exit;
		}

		// unmovables
		final List<Movable> unmovableOwners = new ArrayList<>();
		for (final Unmovable unmovable : worldServer.unmovables) {
			// always add owner in order for correct restore
			unmovableOwners.add(unmovable.owner);

			// don't add exit
			if (unmovable instanceof Exit && worldClient.exit == null) {
				continue;
			}

			unmovable.owner = null;

			worldClient.unmovables.add(unmovable);
		}

		// movables
		final List<User> movableOwners = new ArrayList<>();
		for (final Movable movable : worldServer.movables) {
			// has to send User
			// - do not leak other things
			// - can't serialize socket
			movableOwners.add(movable.owner);
			worldClient.movables.add(movable);

			if (movable.owner != null) {
				final User user = new User();
				user.name = movable.owner.name;
				movable.owner = user;
			}
		}

		// deep copy (the maintainable way)
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(worldClient);

			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					byteArrayOutputStream.toByteArray());
			final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			worldClient = (WorldClient) objectInputStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			throw new Error(e);
		}

		for (int i = 0; i < movableOwners.size(); i++) {
			worldServer.movables.get(i).owner = movableOwners.get(i);
		}

		for (int i = 0; i < unmovableOwners.size(); i++) {
			worldServer.unmovables.get(i).owner = unmovableOwners.get(i);
		}

		return worldClient;
	}
}
