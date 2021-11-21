package server;

import user.UserManager;

public class ServerModel {
	public final WorldServer worldServer = new WorldServer();
	public UserManager<UserServer> userManager;
}
