package dk.itu.mario.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
//import org.apache.commons.math3.exception.OutOfRangeException;
//import org.apache.commons.math3.optim.InitialGuess;
//import org.apache.commons.math3.optim.MaxEval;
//import org.apache.commons.math3.optim.PointValuePair;
//import org.apache.commons.math3.optim.SimpleBounds;
//import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
//import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
//import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
//import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;




import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.level.events.BlockClusterEvent;
import dk.itu.mario.level.events.CannonClusterEvent;
import dk.itu.mario.level.events.CoinClusterEvent;
import dk.itu.mario.level.events.EnemyLineEvent;
import dk.itu.mario.level.events.Event;
import dk.itu.mario.level.events.GapEvent;
import dk.itu.mario.level.events.HillEvent;
import dk.itu.mario.level.events.TubeClusterEvent;


public class EventBasedLevel extends Level {
	 
		private static int MIN_Y = 14;
		private static double groundNoise = 1; 
		private static int END_WIDTH = 4;
		private static int END_HEIGHT = 11;
		 
		public static long lastSeed;

	    private Random random;
	    private PriorityQueue<Event> events = new PriorityQueue<Event>(); 
	    
	    private double ODDS_STRAIGHT = 0.1;
	    private double ODDS_HILL_STRAIGHT = 0.8;
	    private double ODDS_TUBES = 0;
	    private double ODDS_JUMPGAP = 0;
	    private double ODDS_CANNONS = 0.1;
	    private double ODDS_ENEMIES = 0;
	    private double ODDS_COINS = 0;
	    private double ODDS_COIN_BLOCK = 0; // unused
	    private double ODDS_BLOCK = 0;
	    
        private PlayerType myType;
        private GamePlay playerMetrics = null;
	    
	    private double[] pmf = null;
	    
	    public void setPmf() {
	    	pmf = new double[] {ODDS_STRAIGHT, ODDS_HILL_STRAIGHT, ODDS_TUBES, 
				ODDS_JUMPGAP, ODDS_CANNONS, ODDS_ENEMIES, ODDS_COINS, 
				ODDS_COIN_BLOCK, ODDS_BLOCK};
	    }
	    
	    private int[] groundHeights;
	    private Event[] xOccupier;
		
		public EventBasedLevel(int width, int height)
	    {
			super(width, height);
			
			groundHeights = new int[width];
			int numEvents = 150;
			random = new Random();
			xOccupier = new Event[this.width + 10];
			for (int i = 0; i < numEvents; i++) {
				Event sample = sampleEvent();
				if (sample != null) {
					updateXOc(sample);
					events.add(sample);	
				} else {
					System.out.println("Null sample!");
				}
			}
			

	    }

		public EventBasedLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics)
	    {
	        this(width, height);
	        this.playerMetrics = playerMetrics;
			//create a player model based on the current player
            myType = new PlayerType();
            myType.calculateStats();
            //match that player model to one of the existing profiles
            identifyPlayer();

            //the following lines are used to hardcode which type of player it is
            //myType = new NewPlayer();
            //myType = new CoinCollector();
            //myType = new EnemyKiller();
            //myType = new Explorer();
            //myType = new Speeder();
            
            //now myType is what type they are 
            //set the odds now 
            setOdds();
	        random = new Random();
	        
	        buildGround(11, 0, width);
	        
	        renderEvents();
	        xExit = width - END_WIDTH;
	        yExit = groundHeights[xExit];
	    }
		
		/*private static double[][] boundaries(int dim, double lower, double upper) {
			double[][] boundaries = new double[2][dim];
			for (int i = 0; i < dim; i++)
				boundaries[0][i] = lower;
			for (int i = 0; i < dim; i++)
				boundaries[1][i] = upper;
			return boundaries;
		}*/
		
		/*private double[] locs;
		final MultivariateFunction func = new MultivariateFunction() {
			
            public double value(double[] x) {
            	double agg = 1;
                for (int i1 = 0; i1 < x.length; i1++) {
                	for (int i2 = i1; i2 < x.length; i2++) {
                		double distance = Math.abs(x[i1] - x[i2]) + 1;
                		double value = distance * distance;
                		
                		agg *= value;
                	}
                }
                
                return agg;
            }
        };*/
        
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
			if (index == 8)
				ret = createBlockCluster();
			if (index == 6) 
				ret = new CoinClusterEvent(random.nextInt(this.width - END_WIDTH) + 1, random.nextInt(3) + 1, random.nextInt(10), 0.8);
			if (index == 5) 
				ret = new EnemyLineEvent(random.nextInt(this.width - END_WIDTH) + 1, 0, random.nextInt(3) + 1, 5, this);
			if (index == 4) 
				ret = createCannonCluster();
			if (index == 3)
				ret = new GapEvent(random.nextInt(this.width - END_WIDTH) + 1, random.nextInt(5) + 1);
			if (index == 2)
				ret = new TubeClusterEvent(random.nextInt(this.width - END_WIDTH) + 1, 0, random.nextInt(6) + 2);
			if (index == 1) 
				ret = createHillEvent();
			if (index == 0) 
				ret = createHillEvent();
			return ret;
		}
		
		private void updateXOc(Event ev) {
			for (int pos = 0; pos < ev.getLength(); pos++) {
				
				xOccupier[ev.getX() + pos] = ev;
			}
		}
		private BlockClusterEvent createBlockCluster() {
			int x = random.nextInt(this.width - END_WIDTH) + 1;
			int dy = random.nextInt(3) + 2;
			int length = random.nextInt(10);
			int maxDy = 0;
			
			for (int pos = 0; pos < length; pos++) {
				if (x + pos >= this.width)  {
					System.out.println("Out of bounds, returning null!");
					return null;
				}
				if (xOccupier[x + pos] != null) {
					maxDy = Math.max(maxDy, xOccupier[x + pos].getDy());
				}
			}
			
			dy += maxDy;
			BlockClusterEvent event = new BlockClusterEvent(x, dy, length, 0.2, 0.2);
			return event;
		}
		
		private CannonClusterEvent createCannonCluster() {
			int x = -1;
			int dy = 0;
			int length;
			
			// try numTries times then give up
			for (int numTries = 1000; numTries > 0; numTries--) {
				x = random.nextInt(this.width - END_WIDTH) + 1;
				length = random.nextInt(6) + 4;
				boolean succeeded = true;
				boolean subsucceeded = true;
				for (int pos = 1; pos < length; pos++) {
					if (xOccupier[x + pos] != xOccupier[x + pos - 1] || xOccupier[x + pos - 1] == null) {
						subsucceeded = false;
					}
				}
				
				if (subsucceeded) {
					if (x + length - 1 >= this.width)  {
						System.out.println("[CannonCluster] Out of bounds, returning null!");
						return null;
					}
					return new CannonClusterEvent(x, dy + xOccupier[x].getDy(), length);
				}
				
				for (int pos = 0; pos < length; pos++) {
					if (x + pos >= this.width)  {
						System.out.println("[CannonCluster] Out of bounds, returning null!");
						return null;
					}
					if (xOccupier[x + pos] != null) {
						succeeded = false;
						break;
					}
				}
				
				if (succeeded) {
					if (x + length - 1 >= this.width)  {
						System.out.println("[CannonCluster] Out of bounds, returning null!");
						return null;
					}
					return new CannonClusterEvent(x, dy, length);
				}
			}
			
			return null; 
		}
		
		private HillEvent createHillEvent() {
			int x = -1;
			int dy = 0;
			int length;
			
			// try numTries times then give up
			for (int numTries = 1000; numTries > 0; numTries--) {
				x = random.nextInt(this.width - END_WIDTH) + 1;
				length = random.nextInt(5) + 2;
				boolean succeeded = true;
				for (int pos = 0; pos < length; pos++) {
					if (x + pos >= this.width)  {
						System.out.println("[Hill] Out of bounds, returning null!");
						return null;
					}
					if (xOccupier[x + pos] != null) {
						succeeded = false;
						break;
					}
				}
				
				if (succeeded) {
					return new HillEvent(x, dy, length);
				}
			}
			
			return null; 
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
			
			if (ev.getClass() == EnemyLineEvent.class) {
				SpriteTemplate[] temps = ((EnemyLineEvent)ev).getTemplates();
				for (int y = 0; y < grid.length; y++) {
					this.setSpriteTemplate(anchorX + y, groundHeights[anchorX + y] - 2, temps[y]);
				}
			} else {
				for (int y = 0; y < grid.length; y++) {
					for (int x = 0; x < grid[0].length; x++) {
						if (grid[y][x] != 0) {
							this.setBlock(anchorX + x, groundHeights[anchorX] - y - dy - 1, grid[y][x]);
						}
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
	    
	    public void setOdds() {
	    	ODDS_STRAIGHT = 0;
	    	ODDS_HILL_STRAIGHT = 0.4;
	    	ODDS_TUBES = 0.1;
	    	ODDS_JUMPGAP = 0.1;
	    	ODDS_CANNONS = 0.1;
            ODDS_ENEMIES = 0.1;
            ODDS_COINS = 0.1;
            ODDS_COIN_BLOCK = 0;
            ODDS_BLOCK = 0.1;
            
            String myName = myType.getName();
            //code for changing all the odds 
            if (myName.equals("Speeder")) {
            	ODDS_JUMPGAP = 0.2;
                ODDS_TUBES = 0;
            }
            else if (myName.equals("Explorer")) {
                ODDS_HILL_STRAIGHT = 0.6;
                ODDS_CANNONS = 0;
                ODDS_COINS = 0;
            }
            else if (myName.equals("Enemy Killer")) {
                ODDS_ENEMIES = 0.4;
                ODDS_JUMPGAP = 0;
                ODDS_COINS = 0;
                ODDS_HILL_STRAIGHT = 0.3;
            }
            else if (myName.equals("Coin Collector")) {
                ODDS_COINS = 0.5;
                ODDS_HILL_STRAIGHT = 0.2;
                ODDS_JUMPGAP = 0;
                ODDS_CANNONS = 0;
            }
            else { //new player
                ODDS_ENEMIES = 0.05;
                ODDS_JUMPGAP = 0;
                ODDS_HILL_STRAIGHT = 0.55;
            }
        }
	    public void identifyPlayer() {
            //match the player to a player type and sets mytype to that
            //weighted k nearest neighbor
            
            double newPlayerDistance = euclideanDistance(new NewPlayer());
            double coinCollectorDistance = euclideanDistance(new CoinCollector());
              double enemyKillerDistance = euclideanDistance(new EnemyKiller());
            double explorerDistance = euclideanDistance(new Explorer());
            double speederDistance = euclideanDistance(new Speeder());
            
            Map<Double, PlayerType> distances = new HashMap<Double, PlayerType>();
            distances.put(newPlayerDistance, new NewPlayer());
            distances.put(coinCollectorDistance, new CoinCollector());
            distances.put(enemyKillerDistance, new EnemyKiller());
            distances.put(explorerDistance, new Explorer());
            distances.put(speederDistance, new Speeder());
            
            double [] distanceArr = new double[5];
            distanceArr[0] = newPlayerDistance;
            distanceArr[1] = coinCollectorDistance;
            distanceArr[2] = enemyKillerDistance;
            distanceArr[3] = explorerDistance;
            distanceArr[4] = speederDistance;
            
            double minDistance = Integer.MAX_VALUE;
            
            for (int i = 0; i < distanceArr.length; i++) {
                if (distanceArr[i] < minDistance) {
                    minDistance = distanceArr[i];
                }
            }
            
            myType = distances.get(minDistance);
            
        }
        
        public double euclideanDistance(PlayerType p) {
            double sum = 0;
      
            sum += Math.pow((myType.randomJumps/100.0 - p.randomJumps/100.0), 2);
            sum += Math.pow((myType.percentCoinsCollected - p.percentCoinsCollected), 2);
            sum += Math.pow((myType.percentBlocksBroken - p.percentBlocksBroken), 2);
            sum += Math.pow((myType.percentEnemiesKilled - p.percentEnemiesKilled), 2); 
            sum += Math.pow((myType.deaths/3.0 - p.deaths/3.0), 2);
            sum += Math.pow((myType.timeTaken/200.0 - p.timeTaken/200.0), 2);
            
            return Math.sqrt(sum);
        }
        
        public class PlayerType {
            private int randomJumps = 0;
            private double percentCoinsCollected = 0;
            private double percentBlocksBroken = 0;
            private double percentEnemiesKilled = 0;
            private int deaths = 0;
            private int timeTaken = 0;
            String name;
            
            public PlayerType() {
                calculateStats();
            }
            
            public PlayerType(int randomJumps, double percentCoinsCollected, double percentBlocksBroken, 
                    double percentEnemiesKilled, int deaths, int timeTaken, String name) {
                this.randomJumps = randomJumps;
                this.percentCoinsCollected = percentCoinsCollected;
                this.percentBlocksBroken = percentBlocksBroken;
                this.percentEnemiesKilled = percentEnemiesKilled;
                this.deaths = deaths;
                this.timeTaken = timeTaken;
                this.name = name;
            }
            
            private void calculateStats() {
                randomJumps = (int) playerMetrics.aimlessJumps;
                
                percentCoinsCollected = playerMetrics.coinsCollected/100.0;
                percentBlocksBroken = playerMetrics.percentageBlocksDestroyed;
                
                percentEnemiesKilled = (playerMetrics.RedTurtlesKilled + playerMetrics.GreenTurtlesKilled + playerMetrics.GoombasKilled
                    + playerMetrics.CannonBallKilled + playerMetrics.JumpFlowersKilled
                    + playerMetrics.ChompFlowersKilled) / (double) playerMetrics.totalEnemies;
            
                deaths = (int) playerMetrics.timesOfDeathByFallingIntoGap + playerMetrics.timesOfDeathByRedTurtle
                    + playerMetrics.timesOfDeathByGoomba + playerMetrics.timesOfDeathByGreenTurtle 
                    + playerMetrics.timesOfDeathByArmoredTurtle + playerMetrics.timesOfDeathByJumpFlower 
                    + playerMetrics.timesOfDeathByCannonBall + playerMetrics.timesOfDeathByChompFlower;
                
                timeTaken = playerMetrics.completionTime;
            }
            
            public String getName() {
                return name;
            }
            
            

        }
        
        
        public class NewPlayer extends PlayerType {
            public NewPlayer() {
                super(10, 0, 0, 0, 3, 100, "New Player");
            }
            
        }
        
        public class CoinCollector extends PlayerType {
            public CoinCollector() {
                super(0, .70, .10, 0, 0, 100, "Coin Collector");
            }
        }
        
        public class EnemyKiller extends PlayerType {
            public EnemyKiller() {
                super(0, 0, .10, .90, 0, 100, "Enemy Killer");
            }
        }
        
        public class Explorer extends PlayerType {
            public Explorer() {
                super(20, .4, .7, 0, 2, 100, "Explorer");
            }
        }
        
        public class Speeder extends PlayerType {
      
            public Speeder() {
                super(0, 0, 0, 0, 0, 50, "Speeder");
            }
        }


}

/* Old opt code:
 * /*Object[] arr = this.events.toArray();
	        for (int i = 1000000; i > 0; i--) {
	        	int index1 = random.nextInt(arr.length);
	        	int index2 = random.nextInt(arr.length);
	        	Event ev1 = (Event)arr[index1];
	        	Event ev2 = (Event)arr[index2];
	        	if (ev1 != ev2 && ev1.intersects(ev2)) {
	        		ev1.combine(ev2);
	        		System.out.println("i: " + i + ", combining");
	        	}
	        }
	        double[][] bounds = boundaries(events.size(), 0, 1);
	        double[] startPoint = new double[events.size()];
	        
	        int index = 0;
	        for (Event e : events) {
	        	startPoint[index] = e.getX() / (this.width * 1.0);
	        	startPoint[index] = Math.min(startPoint[index] , 1);
	        	index++;
	        }
	        
	        locs = new double[events.size()];
	        for (int i = 0; i < events.size(); i++) {
	        	locs[i] = random.nextDouble();
	        }
	        
	        PointValuePair pair = null;
	        BOBYQAOptimizer optim = new BOBYQAOptimizer(12);
	        try {
	        	pair = optim.optimize(new MaxEval(10000),
        		 	new ObjectiveFunction(func),
        		 	GoalType.MAXIMIZE,
        		 	new InitialGuess(startPoint),
        		 	new SimpleBounds(bounds[0], bounds[1]));
	        }
	        catch(Exception ex) {
	        	System.out.println(ex);
	        }
	        
	        System.out.print(pair);
	        
	        double[] news = pair.getKey();
	        index = 0;
	        for (Event e : events) {
	        	int newX = (int)Math.round(news[index] * this.width);
	        	//e.setX(newX);
	        	index++;
	        }*/
