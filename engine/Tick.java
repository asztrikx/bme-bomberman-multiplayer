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
import helper.Key;
import server.WorldServer;
import user.User;
import world.element.movable.Movable;
import world.element.movable.Player;
import world.element.unmovable.Exit;
import world.element.unmovable.Unmovable;

public class Tick {
	private WorldServer worldServer;
	public long tickCount = 0;

	public Tick(WorldServer worldServer) {
		this.worldServer = worldServer;
	}

	// checks if any CharacterTypeUser if in a winning state and removes them if so
	public List<Player> nextStateWinners() {
		List<Player> players = new ArrayList<>();
		for (Movable movable : worldServer.movables) {
			if (movable instanceof Player) {
				players.add((Player) movable);
			}
		}

		// can't win until all enemies are dead
		if (worldServer.movables.size() - players.size() != 0) {
			return new ArrayList<>();
		}

		// set user state, remove player
		List<Player> playersAtExit = Collision.getCollisions(players, worldServer.exit.position, null, null);
		return playersAtExit;
	}

	// TickCalculateAnimate calculates next texture state from current
	public void nextStateAnimate() {
		// animate
		for (Unmovable unmovable : worldServer.unmovables) {
			unmovable.animation.increase();
		}
		for (Movable movable : worldServer.movables) {
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
		WorldServer nextWorldServer = new WorldServer();
		nextWorldServer.movables = new LinkedList<>(worldServer.movables);
		nextWorldServer.unmovables = new LinkedList<>(worldServer.unmovables);

		for (Unmovable unmovable : worldServer.unmovables) {
			unmovable.nextState(worldServer, nextWorldServer, tickCount);
			if (unmovable.shouldDestroy(tickCount)) {
				unmovable.destroy(worldServer, nextWorldServer, tickCount);
				nextWorldServer.unmovables.remove(unmovable);
			}
		}

		for (Movable movable : worldServer.movables) {
			movable.nextState(worldServer, nextWorldServer, tickCount);
			if (movable.shouldDestroy(tickCount)) {
				movable.destroy(worldServer, nextWorldServer, tickCount);
				nextWorldServer.movables.remove(movable);
			}
		}

		nextStateAnimate();

		List<Player> playersWinning = nextStateWinners();
		if (playersWinning.size() != 0) {
			for (Player player : playersWinning) {
				player.owner.state = User.State.Won;
			}
			// worldServer.movables.removeAll(playersAtExit);
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
		List<Unmovable> collisionObjectS = Collision.getCollisions(worldServer.unmovables, worldServer.exit.position,
				worldServer.exit, null);
		if (collisionObjectS.size() == 0) {
			worldClient.exit = worldServer.exit;
		}

		// unmovables
		for (Unmovable unmovable : worldServer.unmovables) {
			// don't add exit
			if (unmovable instanceof Exit && worldClient.exit == null) {
				continue;
			}

			worldClient.unmovables.add(unmovable);
		}

		// movables
		List<User> owners = new ArrayList<>();
		for (Movable movable : worldServer.movables) {
			// has to send User
			// - do not leak other things
			// - can't serialize socket
			owners.add(movable.owner);
			worldClient.movables.add(movable);

			if (movable.owner != null) {
				User owner = movable.owner;

				movable.owner = new User();
				movable.owner.name = owner.name;
			}
		}

		// deep copy (the maintainable way)
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(worldClient);

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			worldClient = (WorldClient) objectInputStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			throw new Error(e);
		}

		for (int i = 0; i < owners.size(); i++) {
			worldServer.movables.get(i).owner = owners.get(i);
		}

		return worldClient;
	}
}
