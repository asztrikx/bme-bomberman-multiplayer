package world.element;

import helper.Position;
import world.element.Unmovable.ObjectType;

public abstract class WorldElement {
	public Position position = new Position(0, 0);
	public ObjectType type;
	public Animation animation = new Animation(0, 0, 0);
}
