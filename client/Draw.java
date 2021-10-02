package client;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import di.DI;
import helper.Config;
import helper.Logger;
import helper.Position;
import user.User;
import world.element.movable.Movable;
import world.element.movable.Player;
import world.element.unmovable.Unmovable;

public class Draw extends Canvas {
	private WorldClient worldClient;
	private static Config config = (Config) DI.services.get(Config.class);
	private static Logger logger = (Logger) DI.services.get(Logger.class);

	public Draw() {
		super();
	}

	public void setWorldClient(WorldClient worldClient) {
		this.worldClient = worldClient;
	}

	@Override
	public void paint(Graphics graphics) {
		graphics.drawString("Hello", 40, 40);

		// not yet connected
		if (worldClient == null) {
			return;
		}

		// dead or won
		if (worldClient.state != User.State.Playing) {
			gameEnd(graphics);
			return;
		}

		Player characterMe = worldClient.findMe();
		if (characterMe == null) {
			logger.println("Did not receive character from server");
			throw new RuntimeException();
		}

		// offset
		Position offset = new Position(-characterMe.position.y + config.windowHeight / 2,
				-characterMe.position.x + config.worldWidth / 2);

		clear(graphics, 30, 30, 30);

		exit(graphics, offset);

		unmovable(graphics, offset);

		movable(graphics, offset);
	}

	private void gameEnd(Graphics graphics) {
		int r = 0;
		int g = 0;
		int b = 0;
		switch (worldClient.state) {
			case Dead:
				r = 255;
				g = 0;
				b = 0;
				break;
			case Won:
				r = 255;
				g = 255;
				b = 0;
				break;
			default:
				throw new RuntimeException();
		}
		clear(graphics, r, g, b);
	}

	private void clear(Graphics graphics, int r, int g, int b) {
		graphics.setColor(new Color(r, g, b));
		graphics.fillRect(0, 0, config.windowWidth, config.windowHeight);
	}

	private void exit(Graphics graphics, Position offset) {
		if (worldClient.exit == null) {
			return;
		}

		Image image = worldClient.exit.animation.getImage();
		Position position = worldClient.exit.position.shift(offset);
		graphics.drawImage(image, position.x, position.y, config.squaresize, config.squaresize, null);
	}

	public void unmovable(Graphics graphics, Position offset) {
		for (Unmovable unmovable : worldClient.unmovables) {
			Image image = unmovable.animation.getImage();
			Position position = unmovable.position.shift(offset);
			graphics.drawImage(image, position.x, position.y, config.squaresize, config.squaresize, null);
		}
	}

	public void movable(Graphics graphics, Position offset) {
		for (Movable movable : worldClient.movables) {
			Image image = movable.animation.getImage();
			Position position = movable.position.shift(offset);
			graphics.drawImage(image, position.x, position.y, config.squaresize, config.squaresize, null);
		}
	}
}
