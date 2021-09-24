package helper;

public class Position {
	public int y, x;

	public Position(int y, int x) {
		this.y = y;
		this.x = x;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Position)) {
			// TODO java
			throw new Error();
		}

		Position position = (Position) object;

		return x == position.x && y == position.y;
	}
}
