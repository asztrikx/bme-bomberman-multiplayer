package server;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import client.WorldClient;
import di.DI;
import engine.Tick;
import helper.Auth;
import helper.AutoClosableLock;
import helper.Config;
import helper.Key;
import helper.Logger;
import helper.Position;
import network.Listen;
import network.Network;
import user.User;
import user.UserManager;
import world.element.movable.Movable;
import world.element.movable.Player;

public class Server implements AutoCloseable {
	private static Config config = (Config) DI.services.get(Config.class);
	private static Logger logger = (Logger) DI.services.get(Logger.class);
	// must be used for
	// - userManager
	// - worldServer
	private Lock lock = new ReentrantLock();

	// session based states
	private WorldServer worldServer;
	private UserManager<UserServer> userManager;
	private Listen listen;

	// calculate next state of worldServer
	private Tick tick;
	private Timer timer;

	public void listen(int port) throws InterruptedException {
		worldServer = new WorldServer();
		userManager = new UserManager<>();
		listen = new Listen();
		listen.listen(port, (Socket socket) -> {
			try {
				return handshake(socket);
			} catch (Exception e) {
				logger.printf("failed to use port: %s", port);
				return false;
			}
		}, (Object object) -> {
			receive(object);
		});

		// tick start: world calc, connected user update
		tick = new Tick(worldServer, userManager);
		timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
					boolean idk = tick.nextState();
					send();
					// TODO will lock get closed?
					if (!idk) {
						cancel();
					}
				}
			}
		};
		timer.schedule(timerTask, config.tickRate);
	}

	public void waitUntilWin() {
		while (tick.nextStateWinners().size() == 0) {
		}
	}

	@Override
	public void close() throws Exception {
		listen.close();
		timer.cancel();
	}

	public void send() {
		WorldClient worldClient = tick.getWorldClient();

		for (UserServer userServer : userManager.getList()) {
			// state
			worldClient.state = userServer.state;

			// alter user character to be identifiable
			Player playerYou = null;
			for (Movable movable : worldServer.movables) {
				if (!(movable instanceof Player)) {
					continue;
				}

				Player player = (Player) movable;

				// TODO fix
				if (player.owner != userServer) {
					playerYou = player;
					player.you = true;
					continue;
				}
			}

			// send
			try {
				listen.send(userServer.socket, worldClient);
			} catch (IOException e) {
				String ip = Network.getIP(userServer.socket);
				int port = Network.getPort(userServer.socket);
				logger.printf("Couldn't send update to client: %s:%d\n", ip, port);
				logger.println(e.getStackTrace());
			}

			// remove character alter
			if (playerYou != null) {
				playerYou.you = false;
			}
		}
	}

	// registers new user connection, returns it with auth
	public boolean handshake(Socket socket) throws ClassNotFoundException, IOException {
		// get basic info
		String name = (String) listen.receive(socket);
		if (name.length() > config.nameMaxLength) {
			return false;
		}

		// add
		UserServer userServer = new UserServer(socket);
		userServer.state = User.State.Playing;
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			// unique name
			int nameSuffix = 1;
			userServer.name = name;
			while (userManager.findByName(userServer.name) != null) {
				userServer.name = name + nameSuffix;
				nameSuffix++;
			}

			// auth generate
			userServer.auth = new Auth(config.authLength);
			while (userManager.findByAuth(userServer.auth) != null) {
				userServer.auth.regenerate(config.authLength);
			}

			// spawn
			Position position = worldServer.getSpawn(config.spawnPlayerSquareFreeSpace);

			// character insert
			Player player = new Player();
			player.bombCount = config.bombCountStart;
			player.owner = userServer;
			player.position = position;
			player.velocity = config.velocityPlayer;
			worldServer.movables.add(player);

			// add after
			// - unique name generation
			// - unique auth generation
			// - unique spawn generation
			userManager.add(userServer);
		}

		// reply
		// has to be outside of lock
		User user = new User();
		user.auth = userServer.auth;
		user.name = userServer.name;
		listen.send(socket, user);

		return true;
	}

	public void receive(Object object) {
		User userUnsafe = (User) object;

		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			// auth validate
			// - length validation
			if (userUnsafe.auth.length() != config.authLength) {
				return;
			}
			UserServer userServer = userManager.findByAuth(userUnsafe.auth);
			if (userServer == null) {
				return;
			}

			// get Player
			Optional<Movable> movableOptional = worldServer.movables.stream()
					.filter((Movable movable) -> movable.owner == userServer).findFirst();
			// TODO dead state?
			if (movableOptional.isEmpty()) {
				return;
			}
			Movable movable = movableOptional.get();

			// name change
			// - length validation
			if (userUnsafe.name.length() > config.nameMaxLength) {
				return;
			}
			if (!userServer.name.equals(userUnsafe.name)) {
				userServer.name = userUnsafe.name;
			}

			// keys copy
			// - length validation
			if (userUnsafe.keys.length != Key.KeyType.KeyLength) {
				return;
			}
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				movable.keys[i] = userUnsafe.keys[i];
			}
		}
	}
}
