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
		// parse cli
		Map<String, String> parsed = parseCLI(args);
		if (parsed == null) {
			return;
		}
		String configFileName = parsed.get("--config-file");

		// DI init
		DI.init(configFileName);
		Config config = (Config) DI.get(Config.class);
		config.port = Integer.parseInt(parsed.get("--server-port"));

		// start appropriate mode
		if (parsed.containsKey("--server") && parsed.containsKey("--client")) {
			new Client();
			new Server();
		} else if (parsed.containsKey("--server")) {
			new Server();
		} else {
			new Client();
		}
	}

	public static Map<String, String> parseCLI(String[] args) {
		Map<String, Flag.Entry> commands = new HashMap<>();
		commands.put("--server", new Flag.Entry("", true, false, null));
		commands.put("--client", new Flag.Entry("", true, false, null));
		commands.put("--server-port", new Flag.Entry("", false, true, String.valueOf(Config.defaultPort)));
		commands.put("--config-file", new Flag.Entry("", false, false, String.valueOf(Config.defaultConfigFileName)));

		Flag flag = new Flag(commands);
		Optional<Map<String, String>> parsedOrError = flag.parse(args);
		if (!parsedOrError.isPresent()) {
			return null;
		}
		return parsedOrError.get();
	}
}
