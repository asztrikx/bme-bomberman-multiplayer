package flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @formatter:off
 * Logger should not be used here as
 * - it is specific for CLI
 * - logger requires DI to be inited which requires config file which is read from cli (here)
 * @formatter:on
 */
public class Flag {
	private Map<String, Flag.Entry> commands;

	public Flag(final Map<String, Flag.Entry> commands) {
		this.commands = commands;
	}

	public static class Entry {
		public String helpMessage;
		public boolean noParam;
		public boolean required;
		public String defaultValue;

		public Entry(final String helpMessage, final boolean noParam, final boolean required,
				final String defaultValue) {
			this.helpMessage = helpMessage;
			this.noParam = noParam;
			this.required = required;
			this.defaultValue = defaultValue;
		}
	}

	public void printHelp() {
		System.out.println("Available commands");
		commands.forEach((final String name, final Entry entry) -> {
			System.out.println(String.format("%s: %s", name, entry.helpMessage));
		});
	}

	public Optional<Map<String, String>> parse(final String[] args) {
		for (final String arg : args) {
			if (arg.equals("--help")) {
				printHelp();
				return Optional.empty();
			}
		}

		final Map<String, String> parsed = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			if (!commands.containsKey(arg)) {
				System.out.println(String.format("Unknown command: %s", arg));
				return Optional.empty();
			}

			final Entry entry = commands.get(arg);
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
		for (final Map.Entry<String, Entry> mapEntry : commands.entrySet()) {
			final String name = mapEntry.getKey();
			final Entry entry = mapEntry.getValue();

			if (!parsed.containsKey(name)) {
				if (entry.defaultValue == null) {
					if (entry.required) {
						System.out
								.println(String.format("Missing required parameter: %s, %s", name, entry.helpMessage));
						missing = true;
					}
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

	public boolean required(final Map<String, String> parsed, final String... requiredNames) {
		boolean missing = false;
		for (final String requiredName : requiredNames) {
			if (!parsed.containsKey(requiredName)) {
				System.out.println(String.format("Missing required parameter: %s, %s", requiredName,
						commands.get(requiredName).helpMessage));
				missing = true;
			}
		}
		return !missing;
	}
}
