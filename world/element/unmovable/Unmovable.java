package world.element.unmovable;

import world.element.Animation;
import world.element.WorldElement;
import world.element.movable.Movable;

public abstract class Unmovable extends WorldElement {
	public int velocity;
	public Movable owner = null;
	// TODO try to remove this, maybe player can go through walls, because buggy
	// collision
	public boolean movedOutOfBomb = true;

	public Unmovable(final Animation animation) {
		super(animation);
	}
}
