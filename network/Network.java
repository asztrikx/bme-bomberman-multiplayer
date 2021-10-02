package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class Network implements AutoCloseable {
	public void send(Socket socket, Object... objects) throws IOException {
		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		for (Object object : objects) {
			objectOutputStream.writeObject(object);
		}
	}

	public Object receive(Socket socket) throws IOException, ClassNotFoundException {
		InputStream inputStream = socket.getInputStream();
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

		return objectInputStream.readObject();
	}

	public static String getIP(Socket socket) {
		return socket.getInetAddress().getAddress().toString();
	}

	public static int getPort(Socket socket) {
		return socket.getPort();
	}

	abstract public void close() throws Exception;
}