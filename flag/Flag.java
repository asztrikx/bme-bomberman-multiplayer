package flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import di.DI;
import helper.Logger;

public class Flag {
	private static Logger logger = (Logger) DI.get(Logger.class);

	Map<String, Flag.Entry> commands;

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
		logger.println("Available commands");
		commands.forEach((final String name, final Entry entry) -> {
			logger.println(String.format("%s: %s", name, entry.helpMessage));
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
				logger.println(String.format("Unknown command: %s", arg));
				return Optional.empty();
			}

			final Entry entry = commands.get(arg);
			if (!entry.noParam && i + 1 == args.length) {
				logger.println(String.format("Missing parameter for: %s", arg));
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
						logger.println(String.format("Missing required parameter: %s, %s", name, entry.helpMessage));
						missing = true;
					}
				} else {
					logger.println(String.format("Using default value %s for %s", entry.defaultValue, name));
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
				logger.println(String.format("Missing required parameter: %s, %s", requiredName,
						commands.get(requiredName).helpMessage));
				missing = true;
			}
		}
		return !missing;
	}
}
