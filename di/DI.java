package di;

import java.util.HashMap;
import java.util.Map;

public class DI {
	public static Map<Class<? extends Object>, Object> services = new HashMap<>();

	/**
	 * Injected classes may depend on each other so they already have to be on list
	 * when `new` is called
	 * 
	 * @param Class instance
	 */
	public static void put(Object object) {
		services.put(object.getClass(), object);
	}
}
