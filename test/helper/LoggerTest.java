package test.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import helper.Logger;

public class LoggerTest {
	@Test
	public void Print() throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Logger logger = new Logger(byteArrayOutputStream);
		String text = "Never gonna give you up, never gonna let you down";
		logger.print(text);

		assertEquals(text, byteArrayOutputStream.toString());
	}
}
