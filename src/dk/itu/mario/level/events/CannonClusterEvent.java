package dk.itu.mario.level.events;

import java.util.ArrayList;

public class CannonClusterEvent extends Event {
	
	public static final byte CANNON_BOTTOM = (byte)(14 + 2 * 16);
	public static final byte CANNON_MIDDLE = (byte)(14 + 1 * 16);
	public static final byte CANNON_TOP = (byte)(14 + 0 * 16);
	public static final byte MAX_CANNONS = 3;
	
	public CannonClusterEvent(int x, int dy, int length) {
		super(x, dy, length);
		generate();
		this.stackable = true;
		this.stable = false;
		this.priority = 2;
	}

	@Override
	protected void generate() {
		int numCannons = rand.nextInt(MAX_CANNONS) + 1;
		
		ArrayList<Integer> locs = new ArrayList<Integer>();
		for (int i = 0; i < this.length; i++) {
			locs.add(i);
		}
		
		int[] cannonHeights = new int[numCannons];
		int[] cannonLocations = new int[numCannons];
		
		for (int i = 0; i < numCannons; i++) {
			cannonLocations[i] = locs.remove(rand.nextInt(locs.size()));
		}
		
		int height = 0;
		for (int i = 0; i < numCannons; i++) {
			int tHeight = rand.nextInt(4) + 1;
			cannonHeights[i] = tHeight;
			if (tHeight > height)
				height = tHeight;
		}
		
		this.grid = new byte[height][length];
		
		for (int i = 0; i < numCannons; i++) {
			int xloc = cannonLocations[i];
			grid[cannonHeights[i] - 1][xloc] = CANNON_TOP;
			
			if (cannonHeights[i] > 1) {
				grid[cannonHeights[i] - 2][xloc] = CANNON_MIDDLE;
			}
			
			for (int y = 0; y < cannonHeights[i] - 2; y++) {
				grid[y][xloc] = CANNON_BOTTOM;
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
