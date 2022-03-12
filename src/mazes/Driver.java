package mazes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;

public class Driver {

	public static void main(String[] args) {
		
		File folder = new File("C:\\Users\\za6193kq\\OneDrive - MNSCU\\Desktop\\School\\2021 - 2022\\22spring\\CS495\\resources\\mazebenchmark.github.io-master\\mazebenchmark.github.io-master\\dataset\\SCMP");
		File[] subFolders = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith("SCMP");
			}
		});
		for (File sub : subFolders) {
			File locations = sub.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return name.equals("starting_locations.loc");
				}
			})[0];
			File[] mazes = new File(sub.getAbsolutePath() + "/mazes").listFiles();
			for (File maze : mazes) {
				runMazes(locations, maze);
			}
		}
	}

	public static void runMazes(File starts, File maze) {
		Scanner input = null;
		try { 
			input = new Scanner(starts);
			// clear width and height
			input.nextInt();
			input.nextInt();
			int numberOfLocations = input.nextInt();
			for (int i = 0; i < numberOfLocations; i++) {
				int row = input.nextInt();
				int col = input.nextInt();
				MazeRunner runner = new MazeRunner(maze, row, col);
				//System.out.println("Running " + maze.getName() + " at [" + row + ", " + col + "]" + " with Tremaux");
				runner.run(false);
				//System.out.println("Running " + maze.getName() + " at [" + row + ", " + col + "]" + " with Modified");
				runner.run(true);
			}
		} catch (FileNotFoundException e) {}
		finally {
			input.close();
		}
	}
}
