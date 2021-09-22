public class User {
	public boolean[] keys = new boolean[Key.KeyType.KeyLength];
	public String name;
	public Auth auth;

	public enum State {
		Playing, Dead, Won,
	}
}
