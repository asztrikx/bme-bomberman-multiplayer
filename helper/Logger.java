package helper;

import java.io.PrintWriter;
import java.io.Writer;

public class Logger extends PrintWriter {

	public Logger(Writer w) {
		super(w);
	}
}
