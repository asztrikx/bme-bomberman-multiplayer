package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class Network implements AutoCloseable {
	public void send(ObjectOutputStream objectOutputStream, Object... objects) throws IOException {
		objectOutputStream.reset();
		for (Object object : objects) {
			objectOutputStream.writeObject(object);
		}
	}

	public Object receive(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
		return objectInputStream.readObject();
	}

	public static String getIP(Socket socket) {
		InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
		return inetSocketAddress.getAddress().toString();
	}

	public static int getPort(Socket socket) {
		return socket.getPort();
	}

	abstract public void close() throws Exception;

	public static class Connection implements AutoCloseable {
		public ObjectInputStream objectInputStream;
		public ObjectOutputStream objectOutputStream;
		public Socket socket;
		public String ip;
		public int port;

		public Connection(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, Socket socket) {
			this.objectInputStream = objectInputStream;
			this.objectOutputStream = objectOutputStream;
			this.socket = socket;
			this.ip = Network.getIP(socket);
			this.port = Network.getPort(socket);
		}

		@Override
		public void close() throws Exception {
			socket.close();
		}

		@Override
		public String toString() {
			return String.format("%s:%d", ip, port);
		}
	}
}