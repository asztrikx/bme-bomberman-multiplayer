import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainClient {
	public static void main(String[] args) {
		try {
			chatClient();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int port = 55324;

	public static void chatClient() throws IOException {
		try (Socket socket = new Socket("152.66.182.89", port)) {
			OutputStream outputStream = socket.getOutputStream();

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject("hey now");
			objectOutputStream.writeObject("hey now");
			objectOutputStream.writeObject("hey now");

			objectOutputStream.close();
		}
	}
}