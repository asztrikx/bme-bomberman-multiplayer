package server;

import java.net.Socket;

import user.User;

public class UserServer extends User {
	public Socket socket;

	public UserServer(Socket socket) {
		this.socket = socket;
	}
}
