package mazes;

import java.awt.Color;

public enum Edge {
	WALL(Color.black), UNMARKED(Color.white), MARKED_ONCE(Color.green), MARKED_TWICE(Color.red);

	public final Color color;

	private Edge(Color color) {
		this.color = color;
	}
}
