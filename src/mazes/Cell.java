package mazes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Cell {
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private boolean isGoal;
	private boolean isStart;
	private Edge edges[] = new Edge[4]; 

	public Cell(int walls) {
		isGoal = walls > 15;
		isStart = false;
		for (int i = 0; i < edges.length; i++) {
			edges[i] = ((walls >> i) & 1) == 1 ? Edge.WALL : Edge.UNMARKED;	
		}
	}
	
	public int totalWalls() {
		int walls = 0;
		for (Edge edge : edges) {
			if (edge == Edge.WALL) {
				walls++;
			}
		}
		return walls;
	}
	
	public boolean isGoal() {
		return isGoal;
	}

	public Edge getEdge(int index) {
		return edges[index];
	}

	
	public boolean isStart() {
		return isStart;
	}
	
	public void setStart() {
		isStart = true;
		this.pcs.firePropertyChange("start", false, true);
	}
	
	public void setCurrent(boolean val) {
		this.pcs.firePropertyChange("current", !val, val);
	}
	
	public void setMark(int index) {
		if (index < 0) {
			return;
		}
		if (edges[index] == Edge.UNMARKED) {
			edges[index] = Edge.MARKED_ONCE;
			this.pcs.firePropertyChange("marks", Edge.UNMARKED, Edge.MARKED_ONCE);
		} else if (edges[index] == Edge.MARKED_ONCE) {
			edges[index] = Edge.MARKED_TWICE;
			this.pcs.firePropertyChange("marks", Edge.MARKED_ONCE, Edge.MARKED_TWICE);
		} else {
			System.err.println("Cannot mark edge " + index + ": " + edges[index].name());
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void clearMarks() {
		for (int i = 0; i < edges.length; i++) {
			if (edges[i] != Edge.WALL) {
				edges[i] = Edge.UNMARKED;
				this.pcs.firePropertyChange("marks", Edge.UNMARKED, null);
			}
		}
	}
	
	public String toString() {		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < edges.length; i++) {
			if (edges[i] == Edge.WALL) {
				sb.append('1');
			} else {
				sb.append('0');
			}
		}
		return sb.toString();
		
	}
	
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
}
