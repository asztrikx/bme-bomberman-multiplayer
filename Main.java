import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import client.Client;
import di.DI;
import flag.Flag;
import helper.Config;
import server.Server;

public class Main {
	public static class Foo {
		public int id;
		public String name;

		public Foo(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	public static void main(String[] args) throws Exception {
		DI.init();
		Config config = (Config) DI.services.get(Config.class);

		// parse cli
		Map<String, Flag.Entry> commands = new HashMap<>();
		commands.put("--server", new Flag.Entry("", true, false, null));
		commands.put("--client", new Flag.Entry("", true, false, null));
		commands.put("--server-port", new Flag.Entry("", false, true, String.valueOf(Config.defaultPort)));
		Flag flag = new Flag(commands);
		Optional<Map<String, String>> parsedOrError = flag.parse(args);
		if (!parsedOrError.isPresent()) {
			return;
		}
		Map<String, String> parsed = parsedOrError.get();

		// start appropriate mode
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
		if (parsed.containsKey("--client") || !parsed.containsKey("--server")) {
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
	}
}
