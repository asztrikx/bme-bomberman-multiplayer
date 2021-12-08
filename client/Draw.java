package client;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;

import di.DI;
import helper.Config;
import helper.Key;
import helper.Logger;
import helper.Position;
import user.User;
import world.element.movable.Movable;
import world.element.movable.Player;
import world.element.unmovable.Unmovable;

public class Draw extends Canvas {
	private static Config config = (Config) DI.get(Config.class);
	private static Logger logger = (Logger) DI.get(Logger.class);

	private WorldClient worldClient;
	private BufferStrategy strategy;

	/**
	 * @formatter:off
	 * Initializes GUI drawing
	 * @formatter:on
	 */
	public void init() {
		super.createBufferStrategy(2);
		strategy = super.getBufferStrategy();
	}

	/**
	 * @formatter:off
	 * Setter for worldClient
	 * @param worldClient
	 * @formatter:on
	 */
	public void setWorldClient(final WorldClient worldClient) {
		this.worldClient = worldClient;
	}

	/**
	 * @formatter:off
	 * Manages Swing render, delegates render to render(Graphics)
	 * @formatter:on
	 */
	public void render() {
		do {
			do {
				final Graphics graphics = strategy.getDrawGraphics();
				render(graphics);
				graphics.dispose();
			} while (strategy.contentsRestored());
			strategy.show();
		} while (strategy.contentsLost());
	}

	/**
	 * @formatter:off
	 * Renders the current state
	 * Draws gameend screen if win state is on worldClient
	 * @param graphics
	 * @formatter:on
	 */
	private void render(final Graphics graphics) {
		// not yet connected
		if (worldClient == null) {
			return;
		}

		// dead or won
		if (worldClient.state != User.State.Playing) {
			gameEnd(graphics);
			return;
		}

		final Player playerMe = worldClient.findMe();
		if (playerMe == null) {
			logger.println("Did not receive player from server");
			throw new RuntimeException();
		}

		// offset
		final Position offset = new Position(-playerMe.position.y + config.windowHeight / 2 - config.squaresize / 2,
				-playerMe.position.x + config.windowWidth / 2 - config.squaresize / 2);

		clear(graphics, 30, 30, 30);

		exit(graphics, offset);

		unmovable(graphics, offset);

		movable(graphics, offset);

		if (config.debug) {
			graphics.setColor(Color.RED);
			graphics.fillOval(0, 0, 10, 10);
			graphics.fillOval(config.windowWidth / 2, config.windowHeight / 2, 10, 10);
			graphics.fillOval(config.windowWidth / 2 - config.squaresize / 2,
					config.windowHeight / 2 - config.squaresize / 2, 10, 10);
			graphics.fillOval(config.windowWidth, config.windowHeight, 10, 10);
		}
	}

	/**
	 * @formatter:off
	 * Draws game end screen
	 * @param graphics
	 * @formatter:on
	 */
	private void gameEnd(final Graphics graphics) {
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

	/**
	 * @formatter:off
	 * Clears with specified colors
	 * @param graphics
	 * @param r
	 * @param g
	 * @param b
	 * @formatter:on
	 */
	private void clear(final Graphics graphics, final int r, final int g, final int b) {
		graphics.setColor(new Color(r, g, b));
		graphics.fillRect(0, 0, config.windowWidth, config.windowHeight);
	}

	/**
	 * @formatter:off
	 * Renders exit
	 * @param graphics
	 * @param offset position which should be deduced from all points to centralize non origin points
	 * @formatter:on
	 */
	private void exit(final Graphics graphics, final Position offset) {
		if (worldClient.exit == null) {
			return;
		}

		final Image image = worldClient.exit.animation.getImage();
		final Position position = worldClient.exit.position.shift(offset);
		graphics.drawImage(image, position.x, position.y, config.squaresize, config.squaresize, null);
	}

	/**
	 * @formatter:off
	 * Renders unmovables
	 * @param graphics
	 * @param offset position which should be deduced from all points to centralize non origin points
	 * @formatter:on
	 */
	public void unmovable(final Graphics graphics, final Position offset) {
		for (final Unmovable unmovable : worldClient.unmovables) {
			final Image image = unmovable.animation.getImage();
			final Position position = unmovable.position.shift(offset);
			graphics.drawImage(image, position.x, position.y, config.squaresize, config.squaresize, null);
		}
	}

	/**
	 * @formatter:off
	 * Renders movables
	 * @param graphics
	 * @param offset position which should be deduced from all points to centralize non origin points
	 * @formatter:on
	 */
	public void movable(final Graphics graphics, final Position offset) {
		for (final Movable movable : worldClient.movables) {
			final Image image = movable.animation.getImage();
			final Position position = movable.position.shift(offset);

			// flip image if moving to right
			if (!movable.keys[Key.KeyType.KeyLeft.getValue()] && movable.keys[Key.KeyType.KeyRight.getValue()]) {
				graphics.drawImage(image, position.x + config.squaresize, position.y, -config.squaresize,
						config.squaresize, null);
			} else {
				graphics.drawImage(image, position.x, position.y, config.squaresize, config.squaresize, null);
			}

			if (movable.owner != null) {
				final int nameWidth = graphics.getFontMetrics().stringWidth(movable.owner.name);
				final int nameOffset = (config.squaresize - nameWidth) / 2;

				graphics.setColor(Color.WHITE);
				graphics.drawString(movable.owner.name, position.x + nameOffset, position.y - 10);
			}
		}
	}
}
