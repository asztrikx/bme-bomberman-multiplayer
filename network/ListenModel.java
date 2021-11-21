package network;

import java.net.ServerSocket;
import java.util.List;

import network.Network.Connection;

public class ListenModel {
	public List<Connection> connections;
	public ServerSocket serverSocket;
}
