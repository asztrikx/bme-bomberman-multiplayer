import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import client.Client;
import di.DI;
import flag.Flag;
import helper.Config;
import helper.Logger;
import server.Server;
import world.element.AnimationStore;

public class Main {
	public static void main(String[] args) throws Exception {
		DI.put(new Config());
		DI.put(new Logger(System.out));
		DI.put(new AnimationStore());

		Config config = (Config) DI.services.get(Config.class);
		if (config.ip == null) {
			config.ip = config.defaultIP;
		}
		if (config.name == null) {
			config.name = config.defaultName;
		}

		// parse cli
		Map<String, Flag.Entry> commands = new HashMap<>();
		commands.put("--server", new Flag.Entry("", true, false, null));
		commands.put("--client", new Flag.Entry("", true, false, null));
		commands.put("--server-port", new Flag.Entry("", false, true, String.valueOf(config.defaultPort)));
		Flag flag = new Flag(commands);
		Optional<Map<String, String>> parsedOrError = flag.parse(args);
		if (!parsedOrError.isPresent()) {
			return;
		}
		Map<String, String> parsed = parsedOrError.get();

		// start appropiate mode
		config.port = Integer.parseInt(parsed.get("--server-port"));
		Server server = null;
		if (parsed.containsKey("--server")) {
			server = new Server();
			server.listen(config.port);
			if (!parsed.containsKey("--client")) {
				while (true) {
					// TODO this goes without stop
					server.waitUntilWin();
					server.close();
					server.listen(config.port);
				}
			}
		}
		if (parsed.containsKey("--client")) {
			Client client = new Client();
			if (server != null) {
				while (true) {
					server.waitUntilWin();
					server.close();
					server.listen(config.port);
				}
			}
			client.close();
		}
		// TODO close button event
	}
}
