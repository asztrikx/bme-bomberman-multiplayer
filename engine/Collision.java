package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import di.DI;
import helper.Config;
import helper.Position;
import server.WorldServer;
import world.element.WorldElement;
import world.element.movable.Movable;
import world.element.unmovable.Unmovable;

public class Collision {
	private static Config config = (Config) DI.get(Config.class);

	/**
	 * @formatter:off
	 * Tells whether there's a block size collision between positions
	 * @param position1
	 * @param position2
	 * @return
	 * @formatter:on
	 */
	public static boolean doCollide(final Position position1, final Position position2) {
		if (Math.abs(position1.x - position2.x) >= config.squaresize) {
			return false;
		}
		if (Math.abs(position1.y - position2.y) >= config.squaresize) {
			return false;
		}
		return true;
	}

	/**
	 * @formatter:off
	 * Returns list of WorldElements colliding with given WorldElement
	 * @param <E> worldElements type
	 * @param <E2> worldElementRelative type
	 * @param worldElements elements which should be checked for collision
	 * @param position position which to we check collision (squaresize x squaresize dimension)
	 * @param worldElementRelative elements which collision we check
	 * @param collisionDecide filtering function for WorldElements colliding with position; nullable
	 * @return
	 * @formatter:on
	 */
	public static <E extends WorldElement, E2 extends WorldElement> List<E> getCollisions(final List<E> worldElements,
			final Position position, final E2 worldElementRelative, final BiFunction<E2, E, Boolean> collisionDecide) {
		final List<E> listCollision = new ArrayList<>();

		for (final E worldElement : worldElements) {
			if (worldElement == worldElementRelative) {
				continue;
			}

			if (!doCollide(position, worldElement.position)) {
				continue;
			}

			if (collisionDecide != null && !collisionDecide.apply(worldElementRelative, worldElement)) {
				continue;
			}

			listCollision.add(worldElement);
		}

		return listCollision;
	}

	/**
	 * @formatter:off
	 * Get first valid position on the line (from, to) from != to.
	 * @param <E>
	 * @param worldServer
	 * @param from
	 * @param to
	 * @param worldElement we are moving this
	 * @param collisionDecideUnmovable filter function for Unmovables colliding with worldElement; nullable 
	 * @param collisionDecideMovable filter function for Movables colliding with worldElement; nullable
	 * @return first valid position
	 * @formatter:on
	 */
	public static <E extends WorldElement> Position getValidPositionOnLine(final WorldServer worldServer,
			final Position from, final Position to, final E worldElement,
			final BiFunction<E, Unmovable, Boolean> collisionDecideUnmovable,
			final BiFunction<E, Movable, Boolean> collisionDecideMovable) {
		// position difference in abs is always same for y and x coordinate if none of
		// them is zero
		int step = Math.abs(to.y - from.y);
		if (step == 0) {
			step = Math.abs(to.x - from.x);
		}
		final Position current = new Position(from);

		// stays in place
		if (step == 0) {
			return from;
		}

		// walk by discretely with unit vector as there are scenarios where
		// the collision would misbehave if we would only check the arrival position
		// eg: too fast speed would make it able to cross walls
		// eg: squaresize pixel wide diagonal is crossable this way
		final Position unit = new Position(0, 0);
		if (to.y - from.y != 0) {
			unit.y = (to.y - from.y) / Math.abs(to.y - from.y);
		}
		if (to.x - from.x != 0) {
			unit.x = (to.x - from.x) / Math.abs(to.x - from.x);
		}
		for (int i = 0; i < step; i++) {
			// step y
			current.y += unit.y;

			List<Unmovable> unmovableCollisions = getCollisions(worldServer.unmovables, current, worldElement,
					collisionDecideUnmovable);
			List<Movable> movableCollisions = getCollisions(worldServer.movables, current, worldElement,
					collisionDecideMovable);
			if (unmovableCollisions.size() != 0 || movableCollisions.size() != 0) {
				current.y -= unit.y;
			}

			// step x
			current.x += unit.x;

			unmovableCollisions = getCollisions(worldServer.unmovables, current, worldElement,
					collisionDecideUnmovable);
			movableCollisions = getCollisions(worldServer.movables, current, worldElement, collisionDecideMovable);
			if (unmovableCollisions.size() != 0 || movableCollisions.size() != 0) {
				current.x -= unit.x;
			}
		}

		return current;
	}

	private static boolean[][] collisionFreeCountArray;

	/**
	 * @formatter:off
	 * Recursive helper function of getFreeSpaceCount
	 * @param worldServer
	 * @param positionCompressed position scaled to block size
	 * @return
	 * @formatter:on
	 */
	private static int getFreeSpaceCountRecursion(final WorldServer worldServer, final Position positionCompressed) {
		final Position position = new Position(positionCompressed.y * config.squaresize,
				positionCompressed.x * config.squaresize);

		// in calculation or already calculated
		if (collisionFreeCountArray[positionCompressed.y][positionCompressed.x]) {
			return 0;
		}

		// because of map border there will be no overindexing
		// mark invalid positions also to save collision recalculation
		collisionFreeCountArray[positionCompressed.y][positionCompressed.x] = true;

		// position is valid
		final List<Unmovable> collisionUnmovableS = getCollisions(worldServer.unmovables, position, null, null);
		final int collisionCount = collisionUnmovableS.size();

		if (collisionCount != 0) {
			return 0;
		}

		// neighbour positions
		int count = 1; // current position
		final int directionY[] = { 1, -1, 0, 0 };
		final int directionX[] = { 0, 0, 1, -1 };
		for (int i = 0; i < 4; i++) {
			final Position positionCompressNew = new Position(positionCompressed.y + directionY[i],
					positionCompressed.x + directionX[i]);
			count += getFreeSpaceCountRecursion(worldServer, positionCompressNew);
		}

		return count;
	}

	/**
	 * @formatter:off
	 * Determines the number of blocks (squaresize x squaresize) in which there is no WorldElement reachable from position
	 * @param worldServer
	 * @param positionCompressed position scaled to block size
	 * @return
	 * @formatter:on
	 */
	public static int getFreeSpaceCount(final WorldServer worldServer, final Position position) {
		// memory alloc
		collisionFreeCountArray = new boolean[config.worldHeight][config.windowWidth];

		// recursion
		final Position positionCompress = new Position(position.y / config.squaresize, position.x / config.squaresize);
		final int count = getFreeSpaceCountRecursion(worldServer, positionCompress);

		return count;
	}
}
