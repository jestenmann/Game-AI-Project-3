package dk.itu.mario.level.events;

import java.util.ArrayList;

public class HillEvent extends Event {
	
	public static final byte HILL_FILL = (byte) (5 + 9 * 16);
	public static final byte HILL_LEFT = (byte) (4 + 9 * 16);
    public static final byte HILL_RIGHT = (byte) (6 + 9 * 16);
    public static final byte HILL_TOP = (byte) (5 + 8 * 16);
    public static final byte HILL_TOP_LEFT = (byte) (4 + 8 * 16);
    public static final byte HILL_TOP_RIGHT = (byte) (6 + 8 * 16);
	
    private static int MAX_HEIGHT = 4;
    
	public HillEvent(int x, int dy, int length) {
		super(x, dy, length);
		generate();
	}

	@Override
	protected void generate() {
		int height = rand.nextInt(MAX_HEIGHT);
		grid = new byte[height][length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < length; x++) {
				if (y == height - 1) {
					if (x == 0) {
						grid[y][x] = HILL_TOP_LEFT;
					} else if (x == length - 1) {
						grid[y][x] = HILL_TOP_RIGHT;
					} else {
						grid[y][x] = HILL_TOP;
					}
				} else {
					if (x == 0) {
						grid[y][x] = HILL_LEFT;
					} else if (x == length - 1) {
						grid[y][x] = HILL_RIGHT;
					} else {
						grid[y][x] = HILL_FILL;
					}
					
				}
			}
		}
	}

	@Override
	public double prior(double ratio) {
		return 1;
	}

	@Override
	public double likelihoodGiven(Event event) {
		return 0;
	}
}
