package dk.itu.mario.level.events;

public class CoinClusterEvent extends Event {
	private double chance = 0;
	public CoinClusterEvent(int x, int dy, int length, double chance) {
		super(x, dy, length);
		this.chance = chance;
		this.priority = 10;
		this.stable = false;
		this.stackable = false;
		generate();
	}

	@Override
	protected void generate() {
		int height = rand.nextInt(2) + 1;
		this.grid = new byte[height][length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < length; x++) {
				if (rand.nextDouble() < chance)
					this.grid[y][x] = (byte) (2 + 2 * 16);
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
