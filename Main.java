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

		// parse cli
		Map<String, Flag.Entry> commands = new HashMap<>();
		commands.put("--server", new Flag.Entry("", true, false, null));
		commands.put("--client", new Flag.Entry("", true, false, null));
		commands.put("--ip", new Flag.Entry("", false, false, "127.0.0.1"));
		commands.put("--server-port", new Flag.Entry("", false, true, "32469"));
		commands.put("--name", new Flag.Entry("", false, false, null));
		Flag flag = new Flag(commands);
		Optional<Map<String, String>> parsedOrError = flag.parse(args);
		if (!parsedOrError.isPresent()) {
			return;
		}
		Map<String, String> parsed = parsedOrError.get();

		// start appropiate mode
		int serverPort = Integer.parseInt(parsed.get("--server-port"));
		if (parsed.containsKey("--server")) {
			Server server = new Server();
			server.listen(serverPort);
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
			client.connect(ip, serverPort, name);
			client.waitUntilWin();
			client.close();
		}
	}
}
