package helper;

import java.io.Serializable;
import java.security.SecureRandom;

public class Auth implements Serializable {
	private String value;

	public Auth(final int length) {
		regenerate(length);
	}

	/**
	 * @formatter:off
	 * Creates secure random string with length of supplied amount
	 * @param length
	 */
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
