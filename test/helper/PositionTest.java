package test.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import helper.Position;

public class PositionTest {

	@Test
	public void name() {
		Position position = new Position(-1, 1);
		Position position2 = new Position(position);
		assertEquals(position.y, position2.y);
		assertEquals(position.x, position2.x);
		assertTrue(position.equals(position2));

		position.y -= 1;
		assertFalse(position.equals(position2));

		position.sub(position2);
		assertTrue(position.equals(new Position(-2, 1)));

		position = position.sub(position2);

		assertTrue(position.equals(new Position(-1, 0)));
		assertFalse(position2.equals(new Position(-1, 0)));
	}
}
