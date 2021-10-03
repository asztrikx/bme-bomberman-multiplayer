package engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import client.WorldClient;
import di.DI;
import helper.Key;
import server.WorldServer;
import user.User;
import world.element.movable.Enemy;
import world.element.movable.Movable;
import world.element.movable.Player;
import world.element.unmovable.BombFire;
import world.element.unmovable.Exit;
import world.element.unmovable.Unmovable;

public class Tick {
	private static Collision collision = (Collision) DI.services.get(Collision.class);

	private WorldServer worldServer;
	private long tickCount = 0;

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
		List<Player> playersAtExit = collision.getCollisions(players, worldServer.exit.position, null, null);
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
		// this should be calculated first as these objects should not exists in this
		// tick
		List<Unmovable> deletelist = new ArrayList<>();
		for (Unmovable listItemCurrent : worldServer.unmovables) {
			if (listItemCurrent.shouldDestroy(tickCount)) {
				listItemCurrent.destroy(worldServer);
				deletelist.add(listItemCurrent);
			}
		}
		worldServer.unmovables.removeAll(deletelist);

		// must be before character movement as that fixes bumping into wall TODO ?
		for (Movable movable : worldServer.movables) {
			if (movable instanceof Enemy) {
				movable.nextState(worldServer, tickCount);
			}
		}

		// character movement
		// this should be calculated before TickCalculateFireDestroy() otherwise player
		// would be in fire for 1 tick
		// if 2 character is racing for the same spot the first in list wins
		for (Movable movable : worldServer.movables) {
			if (!(movable instanceof Enemy)) {
				movable.nextState(worldServer, tickCount);
			}
		}

		// should be before any destroy
		List<Player> playersWinning = nextStateWinners();
		if (playersWinning.size() != 0) {
			for (Player player : playersWinning) {
				player.owner.state = User.State.Won;
			}
			// worldServer.movables.removeAll(playersAtExit);
			return false;
		}

		// fire tick
		for (Unmovable unmovable : worldServer.unmovables) {
			if (unmovable instanceof BombFire) {
				unmovable.nextState(worldServer, tickCount);
			}
		}

		// player tick
		List<Movable> deaths = new ArrayList<>();
		for (Movable movable : worldServer.movables) {
			if (movable instanceof Player) {
				movable.nextState(worldServer, tickCount);
				if (movable.owner.state == User.State.Dead) {
					deaths.add(movable);
				}
			}
		}
		for (Movable movable : deaths) {
			worldServer.movables.remove(movable);
		}

		nextStateAnimate();

		tickCount++;

		return true;
	}

	public WorldClient getWorldClient() {
		WorldClient worldClient = new WorldClient();

		// remove exit if behind box
		List<Unmovable> collisionObjectS = collision.getCollisions(worldServer.unmovables, worldServer.exit.position,
				worldServer.exit, null);
		if (collisionObjectS.size() == 0) {
			worldClient.exit = worldServer.exit;
		}

		// unmovables
		for (Unmovable unmovable : worldServer.unmovables) {
			// don't add exit
			if (unmovable instanceof Exit && worldServer.exit == null) {
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
