package user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import helper.Auth;
import world.element.WorldElement;

public class UserManager<U extends User> {
	Map<U, List<WorldElement>> userPossession = new HashMap<>();

	public void add(U user) {
		userPossession.put(user, new ArrayList<>());
	}

	public void remove(U user) {
		userPossession.remove(user);
	}

	public void remove(Auth auth) {
		U user = findByAuth(auth);
		remove(user);
	}

	public U findByAuth(Auth auth) {
		for (U user : userPossession.keySet()) {
			if (user.auth.equals(auth)) {
				return user;
			}
		}
		return null;
	}

	public U findByName(String name) {
		Optional<U> user = userPossession.keySet().stream().filter((U userServerOther) -> {
			return userServerOther.name == name;
		}).findFirst();

		if (user.isEmpty()) {
			return null;
		} else {
			return user.get();
		}
	}

	public List<U> getList() {
		return new ArrayList<>(userPossession.keySet());
	}

	public void clear() {
		userPossession.clear();
	}
}
