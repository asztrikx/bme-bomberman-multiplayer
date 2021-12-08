package test.flag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import di.DI;
import flag.Flag;
import helper.Logger;

public class FlagTest {
	private static Flag flag;

	@BeforeAll
	public static void before() {
		DI.put(new Logger(OutputStream.nullOutputStream()));

		Map<String, Flag.Entry> commands = new HashMap<>();
		commands.put("--foo", new Flag.Entry("help message", true, false, null));
		commands.put("--bar", new Flag.Entry("help message", true, false, "default message"));
		commands.put("--baz", new Flag.Entry("help message", true, true, null));
		commands.put("--qux", new Flag.Entry("help message", false, true, null));
		flag = new Flag(commands);

	}

	@Test
	public void Normal() {
		Optional<Map<String, String>> om = flag.parse(new String[] { "--baz", "--qux", "foo" });
		assertTrue(om.isPresent());
		Map<String, String> m = om.get();
		assertTrue(!m.containsKey("--foo"));
		assertEquals(m.get("--bar"), "default message");
		assertEquals(m.get("--baz"), "");
		assertEquals(m.get("--qux"), "foo");
	}

	@Test
	public void Required() {
		Optional<Map<String, String>> om = flag.parse(new String[] { "--qux", "foo" });
		assertTrue(om.isEmpty());
	}

	@Test
	public void ParameterNone() {
		Optional<Map<String, String>> om = flag.parse(new String[] { "--baz", "--qux" });
		assertTrue(om.isEmpty());
	}
}
