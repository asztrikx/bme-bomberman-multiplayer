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

import client.WorldClient;
import helper.AutoClosableLock;
import server.UserServer;
import server.WorldServer;
import world.element.Movable;
import world.element.Unmovable;

public class Listen implements Closeable {
	// lock for sockets and active
	Lock lock = new ReentrantLock();
	List<Socket> sockets = new LinkedList<>();
	boolean active = true;
	int port;

	public Listen(int port) {
		this.port = port;

		// TODO
		// blocking accept => new thread
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					try (ServerSocket serverSocket = new ServerSocket(port)) {
						Socket socket = serverSocket.accept();

						// do not stop if a clients fails to connect
						try {
							// server might be stopping => closing sockets => lock before accepting new
							try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
								if (active) {
									sockets.add(socket);
								} else {
									socket.close();
								}
							}
						} catch (Exception e) {
							// TODO java logger
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					// TODO java logger
					e.printStackTrace();
					// TODO java
					System.exit(1);
				}
			}
		};
		thread.start();
	}

	@Override
	public void close() throws IOException {
		// do not let new sockets to be added to list
		try (AutoClosableLock autoClosableLock = new AutoClosableLock(lock)) {
			for (Socket socket : sockets) {
				socket.close();
			}
			active = false;
		}
	}

	// NetworkSendClient send worldServer to client as WorldClient
	void NetworkSendClient(WorldServer worldServer, UserServer userServer) throws IOException {
		WorldClient worldClient = new WorldClient();

		// state
		worldClient.state = userServer.state;

		// exit
		if (worldServer.exit != null) {
			worldClient.exit = worldServer.exit;
		}

		// objectS
		for (Unmovable object : worldServer.objectList) {
			// remove exit
			if (object.type == Unmovable.ObjectType.ObjectTypeExit && worldServer.exit == null) {
				continue;
			}

			worldClient.objectList.add(object);
		}

		// characterS
		for (Movable character : worldServer.characterList) {
			worldClient.characterList.add(character);
		}

		// send
		for (Socket socket : sockets) {
			OutputStream outputStream = socket.getOutputStream();

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(worldClient);
		}
	}

}
