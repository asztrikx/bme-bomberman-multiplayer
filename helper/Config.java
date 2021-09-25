package helper;

public class Config {
	public int squaresize = 50;
	public int velocity = 6;
	public int velocityEnemy = 2;
	public int windowHeight = 480;
	public int windowWidth = 640;
	public int worldHeight = 9;
	public int worldWidth = 13;
	public double boxRatio = 0.25;
	public double enemyRatio = 0.05;
	public double enemyKeyChangePossibility = 0.015;
	public long tickRate = 1000 / 58;
	public long tickSecond = 1000 / tickRate; // tick count in one second
	public int authLength = 26;
	// TODO GUI or at least cmd line
	public String ip = "";
	public int port = 0;
}
