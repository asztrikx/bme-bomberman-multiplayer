package di;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import helper.Config;
import helper.Logger;
import world.element.AnimationStore;

public class DI {
	private static Map<Class<?>, Object> services = new HashMap<>();

	/**
	 * @formatter:off
	 * Puts the class into a static store to be reachable as singleton.
	 * Injected classes may depend on each other by DI so they already have to be on the list when `new` is called
	 * @param object instance
	 * @formatter:on
	 */
	public static void put(final Object object) {
		services.put(object.getClass(), object);
	}

	public static Object get(final Class<?> c) {
		return services.get(c);
	}

	/**
	 * @formatter:off
	 * Fills DI with basic class instances
	 * @param configFileName
	 * @formatter:on
	 */
	public static void init(String configFileName) {
		Logger logger = new Logger(System.out);
		Config config;
		try {
			config = Config.getConfig(configFileName);
		} catch (IOException e) {
			logger.printf("Could not read %s. Please try to fix this by deleting the file\n", configFileName);
			return;
		}

		DI.put(config);
		DI.put(logger);
		DI.put(new AnimationStore());
	}
}
