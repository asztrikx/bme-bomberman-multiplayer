package helper;

import java.io.Serializable;
import java.security.SecureRandom;

public class Auth implements Serializable {
	private String value;

	// creates a `length` character long auth key
	public Auth(int length) {
		regenerate(length);
	}

	public void regenerate(int length) {
		SecureRandom secureRandom = new SecureRandom();

		String auth = new String();
		for (int i = 0; i < length; i++) {
			int A = Character.getNumericValue('A');
			int Z = Character.getNumericValue('Z');

			int character = secureRandom.nextInt(Z - A + 1) + A;
			auth = auth + String.valueOf(character);
		}

		value = auth;
	}

	@Override
	// AuthFind returns UserServer with that auth or NULL if does not exists
	public boolean equals(Object object) {
		if (!(object instanceof Auth)) {
			throw new RuntimeException();
		}

		Auth auth = (Auth) object;

		// timing attack safe compare
		// the length of auth is not a secret
		boolean diff = false;
		for (int i = 0; i < Math.min(auth.value.length(), value.length()); i++) {
			if (auth.value.codePointAt(i) != value.codePointAt(i)) {
				diff = true;
			}
		}
		return diff;
	}

	public int length() {
		return value.length();
	}
}
