package dk.itu.mario.level;

import java.util.Random;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.engine.sprites.SpriteTemplate;


public class SimpleHeightLevel extends Level{
	//Store information about the level
	 public int ENEMIES = 0; //the number of enemies the level contains
	 public int BLOCKS_EMPTY = 0; // the number of empty blocks
	 public int BLOCKS_COINS = 0; // the number of coin blocks
	 public int BLOCKS_POWER = 0; // the number of power blocks
	 public int COINS = 0; //These are the coins in boxes that Mario collect
	 
	 private static int MIN_Y = 14;
	 private static double groundNoise = 0.95; 
	 
	 public static long lastSeed;

	    Random random;
		
		public SimpleHeightLevel(int width, int height)
	    {
			super(width, height);
	    }


		public SimpleHeightLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics)
	    {
	        this(width, height);
	        random = new Random();
	        
	        buildGround(11);
	    }
		
		private void buildGround(int y) {
			int lastChange = 0;
	        for (int x = 0; x < width; x++) {
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
	        
	        xExit = width - 5;
	        yExit = y;
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
	    	clone.BLOCKS_COINS = BLOCKS_COINS;
	    	clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
	    	clone.BLOCKS_POWER = BLOCKS_POWER;
	    	clone.ENEMIES = ENEMIES;
	    	clone.COINS = COINS;
	    	
	        return clone;

	      }


}
