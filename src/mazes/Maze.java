package mazes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

public class Maze {

	private static final int WALL_WIDTH = 4;
	private static final int CELL_WIDTH = 50;
	public JFrame mainFrame;

	private Cell[][] grid;
	private int connectivity;

	public Maze(File input) {
		createGrid(input);
	}

	public int getConnectivity() {
		return connectivity;
	}

	public Cell getCell(int row, int col) {
		return grid[row][col];
	}

	public int getRows() {
		return grid.length;
	}

	public int getCols() {
		return grid[0].length;
	}

	public void createGrid(File input) {
		Scanner reader = null;
		try {
			reader = new Scanner(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// get width, height
		int w, h;
		w = reader.nextInt();
		h = reader.nextInt();
		connectivity = reader.nextInt();

		// make grid
		grid = new Cell[w][h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				grid[i][j] = new Cell(reader.nextInt());
			}
		}

		reader.close();
	}

	public void initalize(Color currentColor) {
		mainFrame = new JFrame("Maze");
		mainFrame.setBackground(Color.white);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLayout(new GridLayout(grid.length, grid[0].length));
		// make gui grid
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col++) {
				// mark goal
				JLabel label = new JLabel(grid[row][col].isGoal() ? "END" : null);

				// set label properties
				label.setBorder(createMazeBorder(row, col));
				label.setBackground(Edge.UNMARKED.color);
				label.setOpaque(true);
				label.setPreferredSize(new Dimension(CELL_WIDTH, CELL_WIDTH));
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setVerticalAlignment(SwingConstants.CENTER);

				// make label update when cells are marked
				final Integer r = row;
				final Integer c = col;
				grid[row][col].addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("marks")) {
							label.setBorder(createMazeBorder(r, c));
						} else if (evt.getPropertyName().equals("start")) {
							if ((Boolean) (evt.getNewValue())) {
								label.setText("START");
							}
						} else if (evt.getPropertyName().equals("current")) {
							if ((Boolean) (evt.getNewValue())) {
								label.setBackground(currentColor);
							} else {
								label.setBackground(Edge.UNMARKED.color);
							}
						}
					}
				});
				mainFrame.add(label);
			}
		}
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	public Border createMazeBorder(int row, int col) {
		MatteBorder top = BorderFactory.createMatteBorder((grid[row][col].getEdge(Direction.NORTH.index) != Edge.UNMARKED ? (row == 0 ? WALL_WIDTH * 2 : WALL_WIDTH) : 0), 0, 0, 0,
				grid[row][col].getEdge(Direction.NORTH.index).color);
		MatteBorder right = BorderFactory.createMatteBorder(0, 0, 0,
				(grid[row][col].getEdge(Direction.EAST.index) != Edge.UNMARKED ? (col == grid[0].length - 1 ? WALL_WIDTH * 2 : WALL_WIDTH) : 0), grid[row][col].getEdge(Direction.EAST.index).color);
		MatteBorder bottom = BorderFactory.createMatteBorder(0, 0,
				(grid[row][col].getEdge(Direction.SOUTH.index) != Edge.UNMARKED ? (row == grid.length - 1 ? WALL_WIDTH * 2 : WALL_WIDTH) : 0), 0, grid[row][col].getEdge(Direction.SOUTH.index).color);
		MatteBorder left = BorderFactory.createMatteBorder(0, (grid[row][col].getEdge(Direction.WEST.index) != Edge.UNMARKED ? (col == 0 ? WALL_WIDTH * 2 : WALL_WIDTH) : 0), 0, 0,
				grid[row][col].getEdge(Direction.WEST.index).color);
		return BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(right, left),
				BorderFactory.createCompoundBorder(top, bottom)
			);
	}
	public void printGrid() {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				System.out.print(grid[i][j].toString() + " ");
			}
			System.out.println();
		}
	}
}
