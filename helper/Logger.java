package helper;

import java.io.OutputStream;
import java.io.PrintStream;

public class Logger extends PrintStream {
	public Logger(OutputStream outputStream) {
		super(outputStream);
	}
}
