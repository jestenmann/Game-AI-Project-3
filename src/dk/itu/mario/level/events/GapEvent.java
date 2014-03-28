package dk.itu.mario.level.events;

public class GapEvent extends Event {
    
	public GapEvent(int x, int length) {
		super(x, -14, length);
		this.composable = false;
		this.stackable = false;
		this.stable = false;
		this.priority = 5;
		generate();
	}

	@Override
	protected void generate() {
		grid = new byte[15][length];
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < length; x++) {
				grid[y][x] = 1;
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
