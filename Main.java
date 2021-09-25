import client.Client;
import helper.Config;
import helper.Logger;
import server.Server;

public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			// TODO
			System.exit(1);
		}

		Config config = new Config();

		switch (args[1]) {
			case "server":
				serverMode(config);
				break;
			case "client":
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
