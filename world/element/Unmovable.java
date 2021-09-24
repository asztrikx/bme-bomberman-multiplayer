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
		ObjectTypeBomb(0), ObjectTypeBombFire(1), ObjectTypeWall(2), ObjectTypeBox(3), ObjectTypeExit(4);

		// https://stackoverflow.com/a/8157790/4404911
		private final int value;

		private ObjectType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
