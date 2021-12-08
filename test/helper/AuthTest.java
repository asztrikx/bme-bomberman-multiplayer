package test.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.Test;

import helper.Auth;

public class AuthTest {
	@Test
	public void Equals() {
		Auth auth = new Auth(5);
		Auth auth2 = new Auth(6);
		Auth auth3 = new Auth(5);

		assertNotEquals(auth.length(), auth2.length());
		assertEquals(auth.length(), auth3.length());

		auth3.regenerate(6);
		assertNotEquals(auth3.length(), auth.length());
		assertEquals(auth3.length(), auth2.length());
	}
}
