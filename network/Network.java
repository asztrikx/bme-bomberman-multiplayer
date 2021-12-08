package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class Network implements AutoCloseable {
	/**
	 * @formatter:off
	 * Send objects to ObjectOutputStream
	 * Fixes object caching
	 * @param objectOutputStream
	 * @param objects
	 * @throws IOException
	 * @formatter:on
	 */
	public void send(final ObjectOutputStream objectOutputStream, final Object... objects) throws IOException {
		objectOutputStream.reset();
		for (final Object object : objects) {
			objectOutputStream.writeObject(object);
		}
	}

	/**
	 * @formatter:off
	 * Gets object from objectInputStream
	 * @param objectInputStream
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @formatter:on
	 */
	public Object receive(final ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
		return objectInputStream.readObject();
	}

	/**
	 * @formatter:off
	 * Returns IP of socket
	 * @param socket
	 * @return
	 * @formatter:on
	 */
	public static String getIP(final Socket socket) {
		final InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
		return inetSocketAddress.getAddress().toString();
	}

	/**
	 * @formatter:off
	 * Returns port of socket
	 * @param socket
	 * @return
	 * @formatter:on
	 */
	public static int getPort(final Socket socket) {
		return socket.getPort();
	}

	abstract public void close() throws Exception;

	/**
	 * @formatter:off
	 * Manages a connection by grouping ObjectXXStreams and socket together and
	 * calulating ip and port
	 * @formatter:on
	 */
	public static class Connection implements AutoCloseable {
		public ObjectInputStream objectInputStream;
		public ObjectOutputStream objectOutputStream;
		public Socket socket;
		public String ip;
		public int port;

		public Connection(final ObjectInputStream objectInputStream, final ObjectOutputStream objectOutputStream,
				final Socket socket) {
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

		/**
	 	 * @formatter:off
		 * Format "ip:port"
		 * @formatter:on
		 */
		@Override
		public String toString() {
			return String.format("%s:%d", ip, port);
		}
	}
}