import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
	public static void main(String[] args) {
		try {
			chatServer();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static int port = 55324;

	public static void chatServer() throws IOException, ClassNotFoundException {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			try (Socket socket = serverSocket.accept()) {
				InputStream inputStream = socket.getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

				String message = (String) objectInputStream.readObject();
				System.out.println(message);

				objectInputStream.close();
			}
		}
	}
}
