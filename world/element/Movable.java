package world.element;

import helper.Key;
import helper.Position;
import server.UserServer;

public class Movable extends WorldElement {
	public Position position = new Position(0, 0);
	public CharacterType type;
	public int velocity = 0;
	public int bombCount = 0;
	public UserServer owner;
	public Animation animation = new Animation(0, 0, 10);
	public boolean[] keys = new boolean[Key.KeyType.KeyLength];

	public enum CharacterType {
		CharacterTypeUser, CharacterTypeEnemy, CharacterTypeYou,
	}
}
