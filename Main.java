import java.io.IOException;

import client.Client;
import engine.Collision;
import helper.Config;
import helper.Logger;
import server.Server;
import world.element.Animation;
import world.element.AnimationStore;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			// TODO
			System.exit(1);
		}

		Config.Injected = new Config();
		Logger.Injected = new Logger(System.out);
		Collision.Injected = new Collision();
		System.out.println(Config.class.getSimpleName());
		AnimationStore animationStore = new AnimationStore();
		animationStore.addPath("resource/movable/");
		animationStore.addPath("resource/unmovable/");
		Animation.animationStore = animationStore;

		switch (args[0]) {
			case "--server":
				serverMode();
				break;
			case "--client":
				clientMode();
				break;
			default:
				System.exit(1);
				break;
		}
	}

	public static void serverMode() {
		Server server = new Server();
		try {
			server.Listen(Config.Injected.port);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (true) {
		}
	}

	public static void clientMode() {
		Client client = new Client();
		client.connect(Config.Injected.ip);
	}
}
