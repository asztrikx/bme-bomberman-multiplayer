public class Character {
	Position position = new Position(0, 0);
	CharacterType type;
	int velocity = 0;
	int bombCount = 0;
	UserServer owner;
	Animation animation = new Animation(0, 0, 10);
	boolean[] keys = new boolean[Key.KeyType.KeyLength];

	public enum CharacterType {
		CharacterTypeUser, CharacterTypeEnemy, CharacterTypeYou,
	}
}
