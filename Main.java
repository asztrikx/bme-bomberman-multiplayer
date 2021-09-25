import java.io.IOException;

import client.Client;
import helper.Config;
import helper.Logger;
import server.Server;
import world.element.AnimationStore;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			// TODO
			System.exit(1);
		}

		Config config = new Config();
		System.out.println(Config.class.getSimpleName());
		AnimationStore animationStore = new AnimationStore();
		animationStore.addPath("resource/movable/");
		animationStore.addPath("resource/unmovable/");

		switch (args[0]) {
			case "--server":
				serverMode(config);
				break;
			case "--client":
				clientMode(config);
				break;
			default:
				System.exit(1);
				break;
		}
	}

	public static void serverMode(Config config) {
		Logger logger = new Logger(System.out);
		Server server = new Server(config, logger);
		try {
			server.Listen(config.port);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void clientMode(Config config) {
		Client client = new Client();
		client.connect(config.ip);
	}
}
