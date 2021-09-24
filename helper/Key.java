package helper;

public class Key {
	public static enum KeyType {
		KeyUp(0), KeyDown(1), KeyLeft(2), KeyRight(3), KeyBomb(4);

		public static int KeyLength = values().length;

		// https://stackoverflow.com/a/8157790/4404911
		private final int value;

		private KeyType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
