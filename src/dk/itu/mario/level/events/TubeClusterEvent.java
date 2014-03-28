package dk.itu.mario.level.events;

import java.util.ArrayList;

public class TubeClusterEvent extends Event {
	
	public static final byte TUBE_TOP_LEFT = (byte)(10 + 0 * 16);
	public static final byte TUBE_TOP_RIGHT = (byte)(11 + 0 * 16);
	public static final byte TUBE_SIDE_LEFT = (byte)(10 + 1 * 16);
	public static final byte TUBE_SIDE_RIGHT = (byte)(11 + 1 * 16);
	public static final byte CANNON_TOP = (byte)(14 + 0 * 16);
	public static final byte MAX_TUBES = 2;
	
	public TubeClusterEvent(int x, int dy, int length) {
		super(x, dy, length);
		this.priority = 0;
		this.stable = false;
		this.stackable = true;
		generate();
	}

	@Override
	protected void generate() {
		int numTubes = rand.nextInt(MAX_TUBES) + 1;
		
		ArrayList<Integer> locs = new ArrayList<Integer>();
		// length - 1 because we want to make sure that no tubes hang outside the event zone
		for (int i = 0; i < this.length - 1; i++) {
			locs.add(i);
		}
		
		int[] tubeHeights = new int[numTubes];
		int[] tubeLocations = new int[numTubes];
		
		for (int i = 0; i < numTubes; i++) {
			if (locs.size() == 0) {
				tubeLocations[i] = -1;
				break;
			}
			tubeLocations[i] = locs.remove(rand.nextInt(locs.size()));
			locs.remove(new Integer(tubeLocations[i] + 1));
			locs.remove(new Integer(tubeLocations[i] - 1));
		}
		
		int height = 0;
		for (int i = 0; i < numTubes; i++) {
			int tHeight = rand.nextInt(4) + 1;
			tubeHeights[i] = tHeight;
			if (tHeight > height)
				height = tHeight;
		}
		
		this.grid = new byte[height][length];
		
		for (int i = 0; i < numTubes; i++) {
			int xloc = tubeLocations[i];
			if (xloc < 0) {
				continue;
			}
			grid[tubeHeights[i] - 1][xloc] = TUBE_TOP_LEFT;
			grid[tubeHeights[i] - 1][xloc + 1] = TUBE_TOP_RIGHT;
			
			
			for (int y = 0; y < tubeHeights[i] - 1; y++) {
				grid[y][xloc] = TUBE_SIDE_LEFT;
				grid[y][xloc + 1] = TUBE_SIDE_RIGHT;
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
