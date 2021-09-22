public class Thing {
	public Position position = new Position(0, 0);
	public ObjectType type;
	public long created = -1;
	public long destroy = -1;
	public int velocity;
	public Character owner = null;
	public boolean bombOut = true;
	public Animation animation = new Animation(0, 0, 0);

	public enum ObjectType {
		ObjectTypeBomb, ObjectTypeBombFire, ObjectTypeWall, ObjectTypeBox, ObjectTypeExit,
	}
}
