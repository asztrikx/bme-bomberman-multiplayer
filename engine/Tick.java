package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;

import client.WorldClient;
import helper.AutoClosableLock;
import helper.Config;
import helper.Key;
import helper.Logger;
import network.Listen;
import server.UserServer;
import server.WorldServer;
import user.User;
import user.UserManager;
import world.element.unmovable.BombFire;
import world.element.unmovable.Exit;
import world.element.unmovable.Unmovable;
import world.movable.Enemy;
import world.movable.Movable;
import world.movable.Player;

public class Tick extends TimerTask {
	WorldServer worldServer;
	long tickCount = 0;
	Logger logger;
	Config config;
	Lock lock;
	Collision collision;
	Listen listen;
	UserManager<UserServer> userManager;

	public Tick(WorldServer worldServer, Config config, Logger logger, Lock lock, Listen listen,
			UserManager<UserServer> usermanager) {
		this.worldServer = worldServer;
		this.lock = lock;
		this.logger = logger;
		this.listen = listen;
		this.userManager = usermanager;
		this.collision = new Collision(config, logger);
	}

	@Override
	// Tick calculates new frame, notifies users
	public void run() {
		try {
			try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
				nextState();
				send();
			}
		} catch (Exception e) {
			System.exit(1);
		}

		tickCount++;
	}

	// checks if any CharacterTypeUser if in a winning state and removes them if so
	public boolean nextStateWin() {
		List<Player> players = new ArrayList<>();
		for (Movable movable : worldServer.characterList) {
			if (movable instanceof Player) {
				players.add((Player) movable);
			}
		}

		// can't win until all enemies are dead
		if (worldServer.characterList.size() - players.size() != 0) {
			return false;
		}

		// set user state, remove player
		List<Player> playersAtExit = collision.collisionsGet(players, worldServer.exit.position, null, null);
		for (Player player : playersAtExit) {
			player.owner.state = User.State.Won;
		}
		worldServer.characterList.removeAll(playersAtExit);

		return playersAtExit.size() != 0;
	}

	// TickCalculateAnimate calculates next texture state from current
	public void nextStateAnimate() {
		// animate
		for (Unmovable unmovable : worldServer.objectList) {
			// delay
			unmovable.animation.stateDelayTick++;
			if (unmovable.animation.stateDelayTick <= unmovable.animation.stateDelayTickEnd) {
				continue;
			}
			unmovable.animation.stateDelayTick = 0;

			// state next
			unmovable.animation.state++;
			unmovable.animation.state %= TextureSSObject[unmovable.type.getValue()].length;
		}
		for (Movable movable : worldServer.characterList) {
			boolean moving = false;
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				if (movable.keys[i]) {
					moving = true;
					break;
				}
			}
			if (!moving) {
				movable.animation.state = 0;
				movable.animation.stateDelayTick = 0;
				continue;
			}

			// delay
			movable.animation.stateDelayTick++;
			if (movable.animation.stateDelayTick <= movable.animation.stateDelayTickEnd) {
				continue;
			}
			movable.animation.stateDelayTick = 0;

			// state next
			movable.animation.state++;
			movable.animation.state %= TextureSSCharacter[movable.type.getValue()].length;
		}
	}

	// TickCalculate calculates next state from current
	public void nextState() {
		// this should be calculated first as these objects should not exists in this
		// tick
		List<Unmovable> deletelist = new ArrayList<>();
		for (Unmovable listItemCurrent : worldServer.objectList) {
			if (listItemCurrent.shouldDestroy(tickCount)) {
				listItemCurrent.destroy(config, logger, worldServer);
				deletelist.add(listItemCurrent);
			}
		}
		worldServer.objectList.removeAll(deletelist);

		// must be before character movement as that fixes bumping into wall TODO ?
		for (Movable movable : worldServer.characterList) {
			if (movable instanceof Enemy) {
				movable.move(worldServer, tickCount);
			}
		}

		// character movement
		// this should be calculated before TickCalculateFireDestroy() otherwise player
		// would be in fire for 1 tick
		// if 2 character is racing for the same spot the first in list wins
		for (Movable movable : worldServer.characterList) {
			if (!(movable instanceof Enemy)) {
				movable.move(worldServer, tickCount);
			}
		}

		// should be before any destroy
		if (nextStateWin()) {
			cancel();
			return;
		}

		// fire tick
		for (Unmovable unmovable : worldServer.objectList) {
			if (unmovable instanceof BombFire) {
				unmovable.tick(config, logger, worldServer);
			}
		}

		// player tick
		List<Movable> deaths = new ArrayList<>();
		for (Movable movable : worldServer.characterList) {
			if (movable instanceof Player) {
				movable.tick(config, logger, worldServer);
				if (movable.owner.state == User.State.Dead) {
					deaths.add(movable);
				}
			}
		}
		for (Movable movable : deaths) {
			worldServer.characterList.remove(movable);
		}

		nextStateAnimate();
	}

	// sends new world to connected clients
	public void send() {
		WorldClient worldClient = new WorldClient();

		// remove exit if behind box
		List<Unmovable> collisionObjectS = collision.collisionsGet(worldServer.objectList, worldServer.exit.position,
				worldServer.exit, null);
		if (collisionObjectS.size() == 0) {
			worldClient.exit = worldServer.exit;
		}

		// unmovables
		for (Unmovable unmovable : worldServer.objectList) {
			// don't add exit
			if (unmovable instanceof Exit && worldServer.exit == null) {
				continue;
			}

			worldClient.objectList.add(unmovable);
		}

		// movables
		for (Movable movables : worldServer.characterList) {
			worldClient.characterList.add(movables);
		}

		for (UserServer userServer : userManager.getList()) {
			// state
			worldClient.state = userServer.state;

			// alter user character to be identifiable
			Player playerYou = null;
			for (Movable movable : worldServer.characterList) {
				if (!(movable instanceof Player)) {
					continue;
				}

				Player player = (Player) movable;

				if (player.owner != userServer) {
					playerYou = player;
					player.you = true; // TODO no..
					continue;
				}
			}

			// send
			// TODO
			// send(worldServer, userServer);

			// remove character alter
			if (playerYou != null) {
				playerYou.you = false;
			}
		}
	}

}
