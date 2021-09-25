package network;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import client.WorldClient;
import helper.AutoClosableLock;
import server.UserServer;
import user.UserManager;

public class Listen implements Closeable {
	// lock for sockets and active
	public Lock lock = new ReentrantLock();
	public List<Socket> sockets = new LinkedList<>();
	public boolean active = true;
	public int port;

	public Listen(int port, Function<Socket, Boolean> handshake, Consumer<Socket> receive) {
		this.port = port;

		// blocking accept => new thread
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					try (ServerSocket serverSocket = new ServerSocket(port)) {
						Socket socket = serverSocket.accept();

						// do not stop if a clients fails to connect => try here
						try {
							// server might be stopping => closing sockets => lock before accepting new
							try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
								if (active && handshake.apply(socket)) {
									sockets.add(socket);
								} else {
									socket.close();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		};
		thread.start();

		// TODO create new thread for every client: receive;
	}

	@Override
	public void close() throws IOException {
		// do not let new sockets to be added to list
		try {
			try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
				for (Socket socket : sockets) {
					socket.close();
				}
				active = false;
			}
		} catch (Exception e) {
			System.exit(1);
		}
	}

	// sends worldServer to client as WorldClient
	public void send(WorldClient worldClient, UserManager<UserServer> usermanager) throws IOException {
		// send
		// TODO send to correct socket with auth?
		for (Socket socket : sockets) {
			OutputStream outputStream = socket.getOutputStream();

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(worldClient);
		}
	}

}
