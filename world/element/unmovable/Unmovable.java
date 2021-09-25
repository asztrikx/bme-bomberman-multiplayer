package world.element.unmovable;

import world.element.WorldElement;
import world.movable.Movable;

public abstract class Unmovable extends WorldElement {
	public int velocity;
	public Movable owner = null;
	public boolean bombOut = true; // TODO ?
}
