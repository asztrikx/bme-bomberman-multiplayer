package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import client.UserClient;
import helper.Key;
import server.UserServer;

public class Connect {
	String ip;
	int port;
	Socket socket;

	public Connect(String ip, int port) throws UnknownHostException, IOException {
		this.ip = ip;
		this.port = port;
		socket = new Socket(ip, port);
	}

	// TODO maybe send to different port
	public UserClient Init(UserClient userClient) throws IOException, ClassNotFoundException {
		// copy
		UserServer userServer = new UserServer();
		userServer.name = userClient.name;

		// send
		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(userServer);

		InputStream inputStream = socket.getInputStream();
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		userServer = (UserServer) objectInputStream.readObject();

		// apply changes
		// name could be occupied
		// TODO string len limits
		userClient.auth = userServer.auth;
		userClient.name = userServer.name;

		return userClient;
	}

	// Sends userClient to server as UserServer
	void Send(UserClient userClient) throws IOException {
		// create userServer
		UserServer userServer = new UserServer();
		userServer.auth = userClient.auth;
		userServer.name = userClient.name;
		for (int i = 0; i < Key.KeyType.KeyLength; i++) {
			userServer.keys[i] = userClient.keys[i];
		}

		// send
		OutputStream outputStream = socket.getOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(userServer);
	}
}
