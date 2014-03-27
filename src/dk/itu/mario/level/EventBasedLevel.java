package dk.itu.mario.level;

import java.util.ArrayList;
import java.util.Random;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.level.events.CannonClusterEvent;
import dk.itu.mario.level.events.CoinClusterEvent;
import dk.itu.mario.level.events.Event;
import dk.itu.mario.level.events.GapEvent;
import dk.itu.mario.level.events.HillEvent;
import dk.itu.mario.level.events.TubeClusterEvent;


public class EventBasedLevel extends Level {
	 
		private static int MIN_Y = 14;
		private static double groundNoise = 0.80; 
		private static int END_WIDTH = 4;
		private static int END_HEIGHT = 11;
		 
		public static long lastSeed;

	    private Random random;
	    private ArrayList<Event> events = new ArrayList<Event>();
	    
	    private double ODDS_STRAIGHT = 0;
	    private double ODDS_HILL_STRAIGHT = 0.3;
	    private double ODDS_TUBES = 0.2;
	    private double ODDS_JUMPGAP = 0.1;
	    private double ODDS_CANNONS = 0.05;
	    private double ODDS_ENEMIES = 0;
	    private double ODDS_COINS = 0.35;
	    private double ODDS_COIN_BLOCK = 0;
	    private double ODDS_BLOCK = 0;
	    
	    private double[] pmf = {ODDS_STRAIGHT, ODDS_HILL_STRAIGHT, ODDS_TUBES, 
	    						ODDS_JUMPGAP, ODDS_CANNONS, ODDS_ENEMIES, ODDS_COINS, 
	    						ODDS_COIN_BLOCK, ODDS_BLOCK};
	    
	    private int[] groundHeights;
		
		public EventBasedLevel(int width, int height)
	    {
			super(width, height);
			groundHeights = new int[width];
			int numEvents = 100;
			random = new Random();
			
			for (int i = 0; i < numEvents; i++) {
				events.add(sampleEvent());
			}
	    }

		public EventBasedLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics)
	    {
	        this(width, height);
	        random = new Random();
	        
	        buildGround(11, 0, width);
	        renderEvents();
	        xExit = width - END_WIDTH;
	        yExit = groundHeights[xExit];
	    }
		
		private Event sampleEvent() {
			double[] cdf = new double[9];
			
			cdf[0] = pmf[0];
			
			for (int i = 1; i < pmf.length; i++) {
				cdf[i] = cdf[i - 1] + pmf[i];
			}
			
			double draw = random.nextDouble();
			
			int index = -1;
			for (int i = 0; i < cdf.length; i++) {
				if (draw < cdf[i]) {
					index = i;
					break;
				}
			}
			Event ret = null;
			if (index == 6) 
				ret = new CoinClusterEvent(random.nextInt(this.width - END_WIDTH) + 1, random.nextInt(3) + 1, random.nextInt(10), 0.8);
			if (index == 4) 
				ret = new CannonClusterEvent(random.nextInt(this.width - END_WIDTH) + 1, 0, 10);
			if (index == 3)
				ret = new GapEvent(random.nextInt(this.width - END_WIDTH) + 1, random.nextInt(5) + 1);
			if (index == 2)
				ret = new TubeClusterEvent(random.nextInt(this.width - END_WIDTH) + 1, 0, random.nextInt(6) + 2);
			if (index == 1) 
				ret = new HillEvent(random.nextInt(this.width - END_WIDTH) + 1, 0, random.nextInt(15) + 1);
			if (index == 0) 
				ret = new HillEvent(random.nextInt(this.width - END_WIDTH) + 1, 0, random.nextInt(15) + 1);
			return ret;
		}
		
		private void renderEvents() {
			for (Event ev : events) {
				renderEvent(ev);
			}
		}
		
		private void renderEvent(Event ev) {
			byte[][] grid = ev.getGrid();
			int anchorX = ev.getX();
			//int length = ev.getLength();
			int dy = ev.getDy();
			if (ev.getX() + ev.getLength() >= this.width) {
				System.out.println("Invalid Event Placement!");
				return;
			}
			for (int y = 0; y < grid.length; y++) {
				for (int x = 0; x < grid[0].length; x++) {
					if (grid[y][x] != 0) {
						this.setBlock(anchorX + x, groundHeights[anchorX + x] - y - dy - 1, grid[y][x]);
					}
				}
			}
		}
		
		private void buildGround(int y, int startX, int length) {
			int lastChange = 0;
	        for (int x = startX; x < startX + length; x++) {
	        	double dec = random.nextDouble();
	        	int change = 0;
	        	if (dec > groundNoise && (x - lastChange) > 2) {
		        	change = random.nextInt(2) == 0 ? -1 : 1;
	        		y += change;
	        		lastChange = x;
	        	} 
	        	
	        	if (y > MIN_Y) {
	        		y = MIN_Y;
	        		change = 0;
	        	}
	        	
	        	groundHeights[x] = y;
	        	if (change > 0) {
	        		groundHeights[x] -= change;
	        	}
	        	
	        	for (int y1 = y; y1 < height; y1++) {
	        		if (y1 == y) {
	        			if (change < 0) {
	        				setBlock(x, y1, LEFT_UP_GRASS_EDGE);
	        				setBlock(x, ++y1, RIGHT_POCKET_GRASS);
	        			} else if (change == 0) {
	        				setBlock(x, y1, HILL_TOP);
	        			} else {
	        				setBlock(x, y1 - change, RIGHT_UP_GRASS_EDGE);
	        				setBlock(x, y1, LEFT_POCKET_GRASS);	
	        			}
	        				
	        		}
	        		else {
	        			setBlock(x, y1, HILL_FILL);	
	        		}
	        	}
	        }
		}
	    
	    
	    public RandomLevel clone() throws CloneNotSupportedException {

	    	RandomLevel clone=new RandomLevel(width, height);

	    	clone.xExit = xExit;
	    	clone.yExit = yExit;
	    	byte[][] map = getMap();
	    	SpriteTemplate[][] st = getSpriteTemplate();
	    	
	    	for (int i = 0; i < map.length; i++)
	    		for (int j = 0; j < map[i].length; j++) {
	    			clone.setBlock(i, j, map[i][j]);
	    			clone.setSpriteTemplate(i, j, st[i][j]);
	    	}
	    	//clone.BLOCKS_COINS = BLOCKS_COINS;
	    	//clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
	    	//clone.BLOCKS_POWER = BLOCKS_POWER;
	    	//clone.ENEMIES = ENEMIES;
	    	//clone.COINS = COINS;
	    	
	        return clone;

	      }


}
