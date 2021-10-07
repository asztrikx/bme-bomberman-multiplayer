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
		public String defaultValue;

		public Entry(String helpMessage, boolean noParam, boolean required, String defaultValue) {
			this.helpMessage = helpMessage;
			this.noParam = noParam;
			this.required = required;
			this.defaultValue = defaultValue;
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

			if (entry.noParam) {
				parsed.put(arg, "");
			} else {
				parsed.put(arg, args[i + 1]);
			}

			if (!entry.noParam) {
				i++;
			}
		}

		boolean missing = false;
		for (Map.Entry<String, Entry> mapEntry : commands.entrySet()) {
			String name = mapEntry.getKey();
			Entry entry = mapEntry.getValue();

			if (entry.required && !parsed.containsKey(name)) {
				if (entry.defaultValue == null) {
					System.out.println(String.format("Missing required parameter: %s, %s", name, entry.helpMessage));
					missing = true;
				} else {
					System.out.println(String.format("Using default value %s for %s", entry.defaultValue, name));
					parsed.put(name, entry.defaultValue);
				}
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
