package flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Flag {
	Map<String, Flag.Entry> commands;

	public Flag(Map<String, Flag.Entry> commands) {
		this.commands = commands;
	}

	public static class Entry {
		public String helpMessage;
		public boolean noParam;
		public boolean required;

		public Entry(String helpMessage, boolean noParam, boolean required) {
			this.helpMessage = helpMessage;
			this.noParam = noParam;
			this.required = required;
		}
	}

	public void printHelp() {
		System.out.println("Available commands");
		commands.forEach((String name, Entry entry) -> {
			System.out.println(String.format("%s: %s", name, entry.helpMessage));
		});
	}

	public Optional<Map<String, String>> parse(String[] args) {
		for (String arg : args) {
			if (arg.equals("--help")) {
				printHelp();
				return Optional.empty();
			}
		}

		Map<String, String> parsed = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (!commands.containsKey(arg)) {
				System.out.println(String.format("Unknown command: %s", arg));
				return Optional.empty();
			}

			Entry entry = commands.get(arg);
			if (!entry.noParam && i + 1 == args.length) {
				System.out.println(String.format("Missing parameter for: %s", arg));
				return Optional.empty();
			}

			parsed.put(arg, args[i + 1]);

			if (!entry.noParam) {
				i++;
			}
		}

		boolean missing = false;
		for (Map.Entry<String, Entry> mapEntry : commands.entrySet()) {
			String name = mapEntry.getKey();
			Entry entry = mapEntry.getValue();

			if (entry.required && !parsed.containsKey(name)) {
				System.out.println(String.format("Missing required parameter: %s, %s", name, entry.helpMessage));
				missing = true;
			}
		}
		if (missing) {
			return Optional.empty();
		}

		return Optional.of(parsed);
	}

	public boolean required(Map<String, String> parsed, String... requiredNames) {
		boolean missing = false;
		for (String requiredName : requiredNames) {
			if (!parsed.containsKey(requiredName)) {
				System.out.println(String.format("Missing required parameter: %s, %s", requiredName,
						commands.get(requiredName).helpMessage));
				missing = true;
			}
		}
		return !missing;
	}
}
