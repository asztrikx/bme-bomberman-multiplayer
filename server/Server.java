package server;

import java.io.IOException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

import client.WorldClient;
import di.DI;
import engine.Tick;
import engine.gameend.FirstExit;
import helper.Auth;
import helper.Config;
import helper.Key;
import helper.Logger;
import helper.Position;
import network.Listen;
import network.Network.Connection;
import user.User;
import user.UserManager;
import world.element.movable.Movable;
import world.element.movable.Player;

public class Server implements AutoCloseable {
	private static Config config = (Config) DI.get(Config.class);
	private static Logger logger = (Logger) DI.get(Logger.class);

	// session based states
	private ServerModel model = new ServerModel();
	private Listen listen;

	// calculate next state of worldServer
	private Tick tick;
	private Timer timer;
	private Phaser phaser;

	public Server() throws Exception {
		while (true) {
			phaser = new Phaser(0);
			listen(config.port);
			waitUntilWin();
			close();
		}
	}

	public void listen(final int port) throws InterruptedException {
		model.worldServer.generate();
		model.userManager = new UserManager<>();
		listen = new Listen();
		listen.listen(port, (connection) -> {
			try {
				return handshake(connection);
			} catch (final Exception e) {
				logger.printf("server handshake exception with %s\n", connection.toString());
				e.printStackTrace();
				return false;
			}
		}, (final Connection connection, final Object object) -> {
			receive(connection, object);
		}, (final Connection connection) -> {
			synchronized (model) {
				final UserServer userServer = model.userManager.getList().stream()
						.filter(userServerCandidate -> userServerCandidate.connection.equals(connection)).findFirst()
						.get();
				model.userManager.remove(userServer);
				model.worldServer.movables.removeIf(movable -> movable.owner == userServer);
			}
		});

		// tick start: calculate world, update connected users
		tick = new Tick(model.worldServer, new FirstExit());
		timer = new Timer();
		// java can not create TimerTask as lambda
		// https://stackoverflow.com/questions/37970682/passing-lambda-to-a-timer-instead-of-timertask
		final TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				boolean shouldContinue;
				synchronized (model) {
					shouldContinue = tick.nextState();
					send();
				}

				if (!shouldContinue) {
					// has to be before deregister
					cancel();

					// has to be outside lock for it to be closed
					phaser.arriveAndDeregister();
				}
			}
		};
		phaser.register();
		timer.schedule(timerTask, 0, config.tickRate);
	}

	public void waitUntilWin() {
		phaser.awaitAdvance(phaser.getPhase());
	}

	@Override
	public void close() throws Exception {
		listen.close();
		timer.cancel();
		// phaser might not be deregistered as we cancel the timer
	}

	/**
	 * Must be called with lock closed
	 */
	public void send() {
		final WorldClient worldClient = tick.getWorldClient();

		for (final UserServer userServer : model.userManager.getList()) {
			// state
			worldClient.state = userServer.state;

			// alter user character to be identifiable
			Player playerYou = null;
			for (final Movable movable : worldClient.movables) {
				if (!(movable instanceof Player)) {
					continue;
				}

				final Player player = (Player) movable;

				if (player.owner.name.equals(userServer.name)) {
					playerYou = player;
					player.you = true;
				}
			}

			// send
			try {
				listen.send(userServer.connection.objectOutputStream, worldClient);
			} catch (final IOException e) {
				logger.printf("Couldn't send update to client: %s\n", userServer.connection.toString());
			}

			// remove character alter
			if (playerYou != null) {
				playerYou.you = false;
			}
		}
	}

	// registers new user connection, returns it with auth
	public boolean handshake(final Connection connection) throws ClassNotFoundException, IOException {
		// get basic info
		final String name = (String) listen.receive(connection.objectInputStream);
		if (name.length() > config.nameMaxLength) {
			return false;
		}

		// add
		final UserServer userServer = new UserServer(connection);
		userServer.state = User.State.Playing;
		synchronized (model) {
			// unique name
			int nameSuffix = 1;
			userServer.name = name;
			while (model.userManager.findByName(userServer.name) != null) {
				userServer.name = name + nameSuffix;
				nameSuffix++;
			}

			// auth generate
			userServer.auth = new Auth(config.authLength);
			while (model.userManager.findByAuth(userServer.auth) != null) {
				userServer.auth.regenerate(config.authLength);
			}

			// spawn
			final Position position = model.worldServer.getSpawn(config.spawnPlayerSquareFreeSpace);

			// character insert
			final Player player = new Player();
			player.bombCount = config.bombCountStart;
			player.owner = userServer;
			player.position = position;
			player.velocity = config.velocityPlayer;
			model.worldServer.movables.add(player);

			// add after
			// - unique name generation
			// - unique auth generation
			// - unique spawn generation
			model.userManager.add(userServer);
		}

		// reply
		final User user = new User();
		user.auth = userServer.auth;
		user.name = userServer.name;
		listen.send(connection.objectOutputStream, user);

		return true;
	}

	public void receive(final Connection connection, final Object object) {
		final User userUnsafe = (User) object;

		synchronized (model) {
			// auth validate
			// - length validation
			if (userUnsafe.auth.length() != config.authLength) {
				logger.printf("Too long auth from %s\n", connection.toString());
				return;
			}
			final UserServer userServer = model.userManager.findByAuth(userUnsafe.auth);
			if (userServer == null) {
				logger.printf("Auth unknown from %s\n", connection.toString());
				return;
			}

			// get Player
			final Optional<Movable> movableOptional = model.worldServer.movables.stream()
					.filter((final Movable movable) -> movable.owner == userServer).findFirst();
			// TODO dead state?
			if (movableOptional.isEmpty()) {
				return;
			}
			final Movable movable = movableOptional.get();

			// name change
			// - length validation
			if (userUnsafe.name.length() > config.nameMaxLength) {
				logger.printf("Long name from %s\n", connection.toString());
				return;
			}
			if (!userServer.name.equals(userUnsafe.name)) {
				logger.printf("Replacing name from %s to %s from %s\n", userServer.name, userUnsafe.name,
						connection.toString());
				userServer.name = userUnsafe.name;
			}

			// keys copy
			// - length validation
			if (userUnsafe.keys.length != Key.KeyType.KeyLength) {
				logger.printf("Length of keys is wrong %s\n", connection.toString());
				return;
			}
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				movable.keys[i] = userUnsafe.keys[i];
			}
		}
	}
}
