import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import client.Client;
import di.DI;
import engine.Collision;
import flag.Flag;
import helper.Config;
import helper.Logger;
import server.Server;
import world.element.AnimationStore;

public class Main {
	public static void main(String[] args) throws Exception {
		DI.put(new Config());
		DI.put(new Logger(System.out));
		DI.put(new Collision());
		DI.put(new AnimationStore());

		// parse cli
		Map<String, Flag.Entry> commands = new HashMap<>();
		commands.put("--server", new Flag.Entry("", true, false));
		commands.put("--client", new Flag.Entry("", true, false));
		commands.put("--ip", new Flag.Entry("", false, false));
		commands.put("--port", new Flag.Entry("", false, true));
		commands.put("--name", new Flag.Entry("", false, true));
		Flag flag = new Flag(commands);
		Optional<Map<String, String>> parsedOrError = flag.parse(args);
		if (!parsedOrError.isPresent()) {
			return;
		}
		Map<String, String> parsed = parsedOrError.get();

		// start appropiate mode
		int port = Integer.parseInt(parsed.get("--port"));
		if (parsed.containsKey("--server")) {
			Server server = new Server();
			server.listen(port);
			if (!parsed.containsKey("--client")) {
				server.waitUntilWin();
				server.close();
			}
		}
		if (parsed.containsKey("--client")) {
			if (!flag.required(parsed, "--ip", "--name")) {
				return;
			}
			String ip = parsed.get("--ip");
			String name = parsed.get("--name");
			Client client = new Client();
			client.connect(ip, port, name);
			client.waitUntilWin();
			client.close();
		}
	}
}
