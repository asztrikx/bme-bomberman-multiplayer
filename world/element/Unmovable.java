package world.element;

public class Unmovable extends WorldElement {
	public ObjectType type;
	public long created = -1;
	public long destroy = -1;
	public int velocity;
	public Movable owner = null;
	public boolean bombOut = true;
	public Animation animation = new Animation(0, 0, 0);

	public enum ObjectType {
		ObjectTypeBomb, ObjectTypeBombFire, ObjectTypeWall, ObjectTypeBox, ObjectTypeExit,
	}
}
