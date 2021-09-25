package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import engine.Tick;
import helper.Auth;
import helper.AutoClosableLock;
import helper.Config;
import helper.Key;
import helper.Logger;
import helper.Position;
import network.Listen;
import user.User;
import user.UserManager;
import world.movable.Movable;
import world.movable.Player;

public class Server {
	UserManager<UserServer> userManager = new UserManager<>();
	Lock mutex = new ReentrantLock();
	Config config;
	Logger logger;
	WorldServer worldServer;
	Timer timer = null;
	Listen listen = null;
	Tick tick;

	public Server(Config config, Logger logger) {
		this.logger = logger;
		this.config = config;
		this.worldServer = new WorldServer(config, logger); // not critical section
		this.tick = new Tick(worldServer, config, logger, mutex, listen, userManager);
	}

	public void Listen(int port) throws InterruptedException {
		listen = new Listen(port, (Socket socket) -> {
			try {
				return connect(socket);
			} catch (Exception e) {
				logger.println("connect failed");
				e.printStackTrace();
				return false;
			}
		}, (Socket socket) -> {
			try {
				receive(socket);
			} catch (Exception e) {
				logger.println("receive failed");
				e.printStackTrace();
			}
		});

		// tick start: world calc, connected user update
		timer = new Timer();
		timer.schedule(tick, config.tickRate);
		timer.wait();
	}

	public void stop() throws IOException {
		listen.close();
	}

	// ServerConnect register new connection user, returns it with auth
	// userServerUnsafe is not used after return
	public boolean connect(Socket socket) throws IOException, Exception {
		// get basic info
		InputStream inputStream = socket.getInputStream();
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		String name = (String) objectInputStream.readObject();
		if (name.length() > config.nameMaxLength) {
			return false;
		}

		UserServer userServer = new UserServer();
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			userServer.name = name;
			userServer.state = User.State.Playing;

			// id generate
			while (true) {
				Auth auth = new Auth(config.authLength);

				// id exists
				if (userManager.findByAuth(auth) == null) {
					userServer.auth = auth;
					break;
				}
			}

			// spawn
			Position position = worldServer.getSpawn(config, logger);

			// character insert
			Player player = new Player(config, logger);
			player.bombCount = config.bombCountStart;
			player.owner = userServer;
			player.position = position;
			player.velocity = config.velocity;
			worldServer.characterList.add(player);
		}

		// reply
		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(userServer.auth);

		// userServer insert
		userManager.add(userServer);
		return true;
	}

	public void receive(Socket socket) throws Exception {
		InputStream inputStream = socket.getInputStream();
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		UserServer userServerUnsafe = (UserServer) objectInputStream.readObject();

		try (AutoClosableLock autoClosableLock = new AutoClosableLock(mutex)) {
			// auth validate
			// auth's length validation
			// -
			if (userServerUnsafe.auth.length() != config.authLength) {
				return;
			}
			UserServer userServer = userManager.findByAuth(userServerUnsafe.auth);
			if (userServer == null) {
				return;
			}

			// get Player
			List<Movable> movables = worldServer.characterList.stream()
					.filter((Movable movable) -> movable.owner == userServer).collect(Collectors.toList());
			// has to be alive
			if (movables.size() == 0) {
				return;
			}
			assert movables.size() <= 1;
			Movable movable = movables.get(0);

			// name change
			// name's length validation
			if (userServerUnsafe.name.length() > config.nameMaxLength) {
				return;
			}
			if (!userServer.name.equals(userServerUnsafe.name)) {
				userServer.name = userServerUnsafe.name;
			}

			// keys copy
			// keys's length validation
			if (userServerUnsafe.keys.length != Key.KeyType.KeyLength) {
				return;
			}
			for (int i = 0; i < Key.KeyType.KeyLength; i++) {
				movable.keys[i] = userServerUnsafe.keys[i];
			}
		}
	}

}
