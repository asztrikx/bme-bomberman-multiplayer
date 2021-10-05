package helper;

public class Config {
	// TODO should be same between server and client
	public int squaresize = 50;
	public int velocityPlayer = 3;
	public int velocityEnemy = 1;
	// TODO make position etc for others, optimize after like offset variable
	public int windowHeight = 480;
	public int windowWidth = 640;
	public int worldHeight = 9;
	public int worldWidth = 13;
	public double boxRatio = 0.25;
	public double enemyRatio = 0.05;
	public double enemyKeyChangePossibility = 0.0015;
	public long tickRate = 1000 / 58;
	public long tickSecond = 1000 / tickRate; // tick count in one second
	public int authLength = 26;
	public int nameMaxLength = 15;
	public int bombCountStart = 1;
	public int spawnSquareDistanceFromOthers = 3;
	public int spawnPlayerSquareFreeSpace = 3;
}
