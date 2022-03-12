package mazes;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MazeRunner {

	// DEBUG settings are used to display the GUI
	static final boolean DEBUG = true;
	static final int BASE_DELAY = 1;
	private Maze maze;
	private int startRow;
	private int startCol;
	private int endRow;
	private int endCol;
	private int currentRow;
	private int currentCol;
	private Cell currentCell;
	private int steps;
	private String mazeID;

	public MazeRunner(File mazeData, int startingR, int startingC) {
		maze = new Maze(mazeData);
		currentRow = startRow = startingR;
		currentCol = startCol = startingC;
		currentCell = maze.getCell(currentRow, currentCol);
		currentCell.setCurrent(true);
		steps = 0;
		mazeID = mazeData.getName();
		findExit();
	}

	private void findExit() {
		for (int row = 0; row < maze.getRows(); row++) {
			for (int col = 0; col < maze.getCols(); col++) {
				if (maze.getCell(row, col).isGoal()) {
					endRow = row;
					endCol = col;
					return;
				}
			}
		}
	}

	public void run(boolean modified) {
		if (DEBUG) {
			maze.initalize(modified ? Color.ORANGE : Color.BLUE);
		}
		maze.getCell(startRow, startCol).setStart();
		doMaze(modified);
		save(new File("results.csv"), modified ? "Modified" : "Tremaux");
		if (DEBUG) {
			try {
				Thread.sleep(5 * BASE_DELAY);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		reset();
	}

	private void doMaze(boolean isModified) {
		int cameFrom = -1; // the direction we were coming from
		while (!currentCell.isGoal()) {
			if (DEBUG) { // sleep in debug mode
				try {
					// System.out.println("[" + currentCol + ", " + currentRow + "]");
					Thread.sleep(BASE_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (currentCell.totalWalls() == 2 || (steps == 0 && currentCell.totalWalls() == 3)) {
				// path or started in dead end
				for (int i = 0; i < 4; i++) {
					if (i != cameFrom && currentCell.getEdge(i) != Edge.WALL) {
						move(i);
						cameFrom = (i + 2) % 4;
						break;
					}
				}
			} else if (currentCell.totalWalls() == 3) {
				// dead end, turn around
				move(cameFrom);
				cameFrom = (cameFrom + 2) % 4;
			} else {
				// junction
				currentCell.setMark(cameFrom);
				int moveTo;
				if (isModified) {
					moveTo = findSideModified(cameFrom, currentRow, currentCol, currentCell);
				} else {
					moveTo = findSideTremaux(cameFrom, currentCell);
				}
				currentCell.setMark(moveTo);
				move(moveTo);
				cameFrom = (moveTo + 2) % 4;
			}
		}
	}

	/**
	 * Moves the algorithm into a new cell
	 * 
	 * @param dir - the direction to move (as an int)
	 */
	private void move(int dir) {
		currentCell.setCurrent(false);
		if (dir == Direction.NORTH.index) {
			currentRow -= 1;
		} else if (dir == Direction.EAST.index) {
			currentCol += 1;
		} else if (dir == Direction.SOUTH.index) {
			currentRow += 1;
		} else {
			currentCol -= 1;
		}
		steps++;
		currentCell = maze.getCell(currentRow, currentCol);
		currentCell.setCurrent(true);
	}

	/**
	 * Chooses which way to go based off of Tremaux's Algorithm
	 * 
	 * @param cameFrom - the direction we camefrom, as an int
	 * @param cell     - the current cell, a junction
	 * @return the direction the algorithm should go, as an int
	 */
	private int findSideTremaux(int cameFrom, Cell cell) {
		/*
		 * Tremaux Algorithm:
		 * - Never enter a path that has two marks
		 * - If you arrive at a junction with no marks (besides where you came from)
		 * 	 then choose a random unmarked path
		 * - Else if the path you can from has only 1 mark
		 *   then turn around
		 * - Else randomly choose the path with the fewest marks
		 */
		if (isUnmarkedJunction(cameFrom, cell)) {
			// if it is an unmarked junction, pick a new path at random
			int random = (int) (Math.random() * 4); // [0, 4)
			// else, attempt pick a path that has not already been traversed
			for (int index = random; index < random + 4; index++) {
				int i = index % 4;
				if (i != cameFrom && cell.getEdge(i) == Edge.UNMARKED) {
					return i;
				}
			}
		} else if (cell.getEdge(cameFrom) == Edge.MARKED_ONCE) {
			// if the path we came from only has 1 mark (i.e., we are not returning from a
			// dead end), turn around
			return cameFrom;
		} else {
			// tremaux picks randomly
			int random = (int) (Math.random() * 4); // [0, 4)
			// else, attempt pick a path that has not already been traversed
			for (int index = random; index < random + 4; index++) {
				int i = index % 4;
				if (i != cameFrom && cell.getEdge(i) == Edge.UNMARKED) {
					return i;
				}
			}
			// failing that, pick a path that has only been traversed once
			for (int index = random; index < random + 4; index++) {
				int i = index % 4;
				if (i != cameFrom && cell.getEdge(i) == Edge.MARKED_ONCE) {
					return i;
				}
			}
		}
		System.err.println("Tremaux Logic Error");
		return -1; // should be unreachable
	}

	/**
	 * isUnmarkedJunction
	 * @param cameFrom - the edge the algorithm arrived at the junction from
	 * @param cell - the cell containing the junction
	 * @return returns true if the only marked path is the path we came from
	 */
	private boolean isUnmarkedJunction(int cameFrom, Cell cell) {
		for (int i = 0; i < 4; i++) {
			if (i != cameFrom && cell.getEdge(i) != Edge.UNMARKED && cell.getEdge(i) != Edge.WALL) {
				return false;
			}
		}
		return true;
	}

	private int findSideModified(int cameFrom, int row, int col, Cell cell) {
		/*
		 * Modified Algorithm:
		 * - Never enter a path that has two marks
		 * - If you arrive at a junction with no marks (besides where you came from)
		 * 	 then choose an unmarked path, 
		 *   favoring paths that go towards the exit
		 * - Else if the path you can from has only 1 mark
		 *   then turn around
		 * - Else choose a path with the fewest marks
		 *   favoring paths that go towards the exit
		 */
		ArrayList<Integer> possible = new ArrayList<Integer>();
		if (isUnmarkedJunction(cameFrom, cell)) {
			for (int i = 0; i < 4; i++) {
				if (i != cameFrom && cell.getEdge(i) != Edge.WALL) {
					possible.add(i);
				}
			}
			return chooseSideModified(possible, row, col);
		} else if (cell.getEdge(cameFrom) == Edge.MARKED_ONCE) {
			return cameFrom;
		} else {
			for (int i = 0; i < 4; i++) {
				if (i != cameFrom && cell.getEdge(i) == Edge.UNMARKED) {
					possible.add(i);
				}
			}
			if (possible.size() > 0) {
				return chooseSideModified(possible, row, col);
			} else {
				for (int i = 0; i < 4; i++) {
					if (i != cameFrom && cell.getEdge(i) == Edge.MARKED_ONCE) {
						possible.add(i);
					}
				}
				return chooseSideModified(possible, row, col);
			}
		}
	}
	
	public int chooseRandomly(ArrayList<Integer> possible) {
		int random = (int) (Math.random() * possible.size()); // [0, size]
		return possible.get(random);
	}
	
	public int chooseSideModified(ArrayList<Integer> possible, int row, int col) {
		if (possible.size() == 1) {
			return possible.get(0);
		}
		ArrayList<Integer> ideal = new ArrayList<Integer>();
		// see if any edge directly betters the row or col
		for (int dir : possible) {
			if (dir == Direction.NORTH.index && row > endRow ||
				dir == Direction.SOUTH.index && row < endRow ||
				dir == Direction.EAST.index && col < endCol ||
				dir == Direction.WEST.index && col > endCol ) {
				ideal.add(dir); // dir directly reduces the distance to the end
			}
		}
		if (ideal.size() > 0) {
			return chooseRandomly(ideal);
		} else {
		// else pick any edge tdoes not go directly away from the exit
			for (int dir : possible) {
				if (dir == Direction.NORTH.index && col != endCol ||
					dir == Direction.SOUTH.index && col != endCol ||
					dir == Direction.EAST.index && row != endRow ||
					dir == Direction.WEST.index && row != endRow ) {
					ideal.add(dir); // dir does not directly move away from the exit
				}
			}
			return chooseRandomly(ideal);
		}
	}

	/**
	 * Exports a completed run to a .csv file
	 * 
	 * @param output - the file to output to
	 * @param alg    - which algorithm this run represents
	 */
	private void save(File output, String alg) {
		try {
			FileWriter writer;
			// if file does not already exist
			if (!output.exists()) {
				writer = new FileWriter(output, true);
				writer.append("Algorithm,Connectivity,Steps,File,Start\n");
			} else {
				writer = new FileWriter(output, true);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(alg);
			sb.append(',');
			sb.append(maze.getConnectivity());
			sb.append(',');
			sb.append(steps);
			sb.append(',');
			sb.append(mazeID);
			sb.append(",\"[");
			sb.append(startRow);
			sb.append(", ");
			sb.append(startCol);
			sb.append("\"]");	
			sb.append('\n');
			writer.append(sb.toString());
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Resets the maze back to the freshly imported version
	 */
	public void reset() {
		if (DEBUG) {
			maze.mainFrame.dispose();
		}
		for (int i = 0; i < maze.getRows(); i++) {
			for (int j = 0; j < maze.getCols(); j++) {
				maze.getCell(i, j).clearMarks();
				maze.getCell(i, j).setCurrent(false);
			}
		}
		steps = 0;
		currentRow = startRow;
		currentCol = startCol;
		currentCell = maze.getCell(startRow, startCol);
		currentCell.setCurrent(true);
	}
}
