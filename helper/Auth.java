package helper;

import java.io.Serializable;
import java.security.SecureRandom;

public class Auth implements Serializable {
	private String value;

	// creates a `length` character long auth key
	public Auth(final int length) {
		regenerate(length);
	}

	public void regenerate(final int length) {
		final SecureRandom secureRandom = new SecureRandom();

		String auth = new String();
		for (int i = 0; i < length; i++) {
			final char A = 'A';
			final char Z = 'Z';

			final int character = secureRandom.nextInt(Z - A + 1) + A;
			auth = auth + Character.toString(character);
		}

		value = auth;
	}

	@Override
	// AuthFind returns UserServer with that auth or NULL if does not exists
	public boolean equals(final Object object) {
		if (!(object instanceof Auth)) {
			throw new RuntimeException();
		}

		final Auth auth = (Auth) object;

		// timing attack safe compare
		// the length of auth is not a secret
		boolean diff = false;
		for (int i = 0; i < Math.min(auth.value.length(), value.length()); i++) {
			if (auth.value.codePointAt(i) != value.codePointAt(i)) {
				diff = true;
			}
		}
		return !diff;
	}

	public int length() {
		return value.length();
	}
}
