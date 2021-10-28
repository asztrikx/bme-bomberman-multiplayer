package helper;

public class Key {
	public enum KeyType {
		KeyUp(0), KeyDown(1), KeyLeft(2), KeyRight(3), KeyBomb(4);

		public static int KeyLength = values().length;

		private final int value;

		private KeyType(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
