import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import client.Client;
import di.DI;
import flag.Flag;
import helper.Config;
import server.Server;

public class Main {
	public static void main(String[] args) throws Exception {
		// DI init
		DI.init();
		Config config = (Config) DI.services.get(Config.class);

		// parse cli
		Map<String, String> parsed = parseCLI(args);
		config.port = Integer.parseInt(parsed.get("--server-port"));

		// start appropriate mode
		if (parsed.containsKey("--server") && parsed.containsKey("--client")) {
			Server server = new Server();
			server.listen(config.port); // TODO move to ctor

			new Client();

			// TODO move inside
			while (true) {
				server.waitUntilWin();
				server.close();
				server.listen(config.port);
			}

			// client.close(); // TODO is this needed
		} else if (parsed.containsKey("--server")) {
			Server server = new Server();
			server.listen(config.port); // TODO move to ctor

			while (true) {
				// TODO this goes without stop
				server.waitUntilWin();
				server.close();
				server.listen(config.port);
			}
		} else {
			new Client();
		}
	}

	public static Map<String, String> parseCLI(String[] args) {
		Map<String, Flag.Entry> commands = new HashMap<>();
		commands.put("--server", new Flag.Entry("", true, false, null));
		commands.put("--client", new Flag.Entry("", true, false, null));
		commands.put("--server-port", new Flag.Entry("", false, true, String.valueOf(Config.defaultPort)));

		Flag flag = new Flag(commands);
		Optional<Map<String, String>> parsedOrError = flag.parse(args);
		if (!parsedOrError.isPresent()) {
			return null;
		}
		return parsedOrError.get();
	}
}
