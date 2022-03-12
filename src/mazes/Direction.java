package mazes;

public enum Direction {
	NORTH(0), EAST(1), SOUTH(2), WEST(3);
	
	public final int index;

	private Direction(int index) {
		this.index = index;
	}
}
