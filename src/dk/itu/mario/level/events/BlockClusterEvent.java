package dk.itu.mario.level.events;

public class BlockClusterEvent extends Event {
	private double chanceCoin = 0;
	private double chancePower = 0;
	private double chance = 0;
	
    protected static final byte BLOCK_EMPTY	= (byte) (0 + 1 * 16);
    protected static final byte BLOCK_POWERUP	= (byte) (4 + 2 + 1 * 16);
    protected static final byte BLOCK_COIN	= (byte) (4 + 1 + 1 * 16);
	public BlockClusterEvent(int x, int dy, int length, double chanceCoin, double chancePower) {
		super(x, dy, length);
		this.chance = 0.95;
		this.chanceCoin = chanceCoin;
		this.chancePower = chancePower;
		this.priority = 9;
		this.stackable = true;
		this.stable = false;
		generate();
	}

	@Override
	protected void generate() {
		int height = 1;

		boolean containsPower = rand.nextDouble() < chancePower;

		this.grid = new byte[height][length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < length; x++) {
				if (rand.nextDouble() < chance) {
					if (rand.nextDouble() < chanceCoin) {
						this.grid[y][x] = BLOCK_COIN;
					} else {
						this.grid[y][x] = BLOCK_EMPTY;
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
