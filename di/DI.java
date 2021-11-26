package di;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import helper.Config;
import helper.Logger;
import world.element.AnimationStore;

public class DI {
	private static Map<Class<? extends Object>, Object> services = new HashMap<>();

	/**
	 * Injected classes may depend on each other so they already have to be on list
	 * when `new` is called
	 * 
	 * @param Class instance
	 */
	public static void put(final Object object) {
		services.put(object.getClass(), object);
	}

	public static Object get(final Class<? extends Object> c) {
		return services.get(c);
	}

	public static void init() {
		Logger logger = new Logger(System.out);
		Config config;
		try {
			config = Config.getConfig();
		} catch (IOException e) {
			logger.printf("Could not read %s. Please try to fix this by deleting the file\n", Config.configFileName);
			return;
		}

		DI.put(config);
		DI.put(logger);
		DI.put(new AnimationStore());
	}
}
