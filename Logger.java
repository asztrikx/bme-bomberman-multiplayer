import java.io.Writer;
import java.io.PrintWriter;

public class Logger extends PrintWriter {

	public Logger(Writer w) {
		super(w);
	}
}
