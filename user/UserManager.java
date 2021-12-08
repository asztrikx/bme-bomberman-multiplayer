package user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import helper.Auth;
import world.element.WorldElement;

/**
 * Manages the WorldElements controlled or created by a User
 */
public class UserManager<U extends User> {
	private Map<U, List<WorldElement>> userPossession = new HashMap<>();

	/**
	 * @formatter:off
	 * Adds user
	 * @param user
	 * @formatter:on
	 */
	public void add(final U user) {
		userPossession.put(user, new ArrayList<>());
	}

	/**
	 * @formatter:off
	 * Removes user
	 * @param user
	 * @formatter:on
	 */
	public void remove(final U user) {
		userPossession.remove(user);
	}

	/**
	 * @formatter:off
	 * Removes user based on auth
	 * @param auth
	 * @formatter:on
	 */
	public void remove(final Auth auth) {
		final U user = findByAuth(auth);
		remove(user);
	}

	/**
	 * @formatter:off
	 * Finds user by its auth
	 * @param auth
	 * @return
	 * @formatter:on
	 */
	public U findByAuth(final Auth auth) {
		for (final U user : userPossession.keySet()) {
			if (user.auth.equals(auth)) {
				return user;
			}
		}
		return null;
	}

	/**
	 * @formatter:off
	 * Finds user by its name
	 * @param name
	 * @return
	 * @formatter:on
	 */
	public U findByName(final String name) {
		final Optional<U> user = userPossession.keySet().stream().filter((final U userServerOther) -> {
			return userServerOther.name.equals(name);
		}).findFirst();

		if (user.isEmpty()) {
			return null;
		} else {
			return user.get();
		}
	}

	/**
	 * @formatter:off
	 * Returns Users as list
	 * @return
	 * @formatter:on
	 */
	public List<U> getList() {
		return new ArrayList<>(userPossession.keySet());
	}

	/**
	 * @formatter:off
	 * Removes all user
	 * @formatter:on
	 */
	public void clear() {
		userPossession.clear();
	}
}
