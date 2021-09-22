import java.util.ArrayList;
import java.util.List;

public class UserManager<U extends User> {
	List<U> users = new ArrayList<>();

	public void add(U user) {
		users.add(user);
	}

	public void remove(U user) {
		users.remove(user);
	}

	public void remove(Auth auth) {
		U user = findByAuth(auth);
		users.remove(user);
	}

	public U findByAuth(Auth auth) {
		for (U user : users) {
			if (user.auth.equals(auth)) {
				return user;
			}
		}
		return null;
	}

	public List<U> getList() {
		return users;
	}

	public void clear() {
		users.clear();
	}
}
