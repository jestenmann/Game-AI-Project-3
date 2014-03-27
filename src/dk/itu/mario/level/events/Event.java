package dk.itu.mario.level.events;

import java.awt.Rectangle;
import java.util.Random;

public abstract class Event {
	private static int Z_COUNTER = 0;
	
	private int x;
	private int dy;
	protected int length;
	protected byte[][] grid;
	protected Random rand;
	protected boolean composable = false;
	protected int zIndex = 0;
	
	public Event(int x, int dy, int length) {
		this.x = x;
		this.dy = dy;
		this.length = length;
		this.rand = new Random();
		this.zIndex = Z_COUNTER++;
	}
	
	protected abstract void generate();
	
	public abstract double prior(double ratio);
	
	public abstract double likelihoodGiven(Event event);
	
	public boolean compose(Event b) {
		boolean success = false;
		if (composable) {
			
		} else {
			
		}
		
		return success;
	}
	
	public boolean intersects(Event b) {
		Rectangle myRect = new Rectangle(x, dy, length, grid.length);
		Rectangle theirRect = new Rectangle(b.x, b.dy, b.length, b.grid.length);
		return myRect.intersects(theirRect);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getDy() {
		return dy;
	}

	public void setDy(int dy) {
		this.dy = dy;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[][] getGrid() {
		return grid;
	}

	public void setGrid(byte[][] grid) {
		this.grid = grid;
	}
}
