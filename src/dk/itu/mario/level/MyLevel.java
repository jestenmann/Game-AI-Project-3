package dk.itu.mario.level;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.engine.sprites.SpriteTemplate;


public class MyLevel extends Level{
    //Store information about the level
     public   int ENEMIES = 0; //the number of enemies the level contains
     public   int BLOCKS_EMPTY = 0; // the number of empty blocks
     public   int BLOCKS_COINS = 0; // the number of coin blocks
     public   int BLOCKS_POWER = 0; // the number of power blocks
     public   int COINS = 0; //These are the coins in boxes that Mario collect
 
 
    private static Random levelSeedRandom = new Random();
        public static long lastSeed;
        
        Random random;

        //added this 
        private static final int ODDS_STRAIGHT = 0;
        private static final int ODDS_HILL_STRAIGHT = 1;
        private static final int ODDS_TUBES = 2;
        private static final int ODDS_JUMPGAP = 3;
        private static final int ODDS_CANNONS = 4;
        private static final int ODDS_ENEMIES = 5;
        private static final int ODDS_COINS = 6;
        private static final int ODDS_COIN_BLOCK = 7;
        private static final int ODDS_BLOCK = 8;
      
        private PlayerType myType;
        private GamePlay playerMetrics = null;
        
        private int[] odds = new int[9];
        
        private int totalOdds;
  
        private int difficulty;
        private int type;
        private int gaps;
        
        public MyLevel(int width, int height)
        {
            super(width, height);
        }


        public MyLevel(int width, int height, long seed, int difficulty, int type, GamePlay playerMetrics)
        {
            
            this(width, height);
            this.playerMetrics = playerMetrics;
            creat(seed, difficulty, type);
        }

        public MyLevel(int width, int height, long seed, int difficulty, int type)
        {
            this(width, height);
            creat(seed, difficulty, type);
        }

         public void creat(long seed, int difficulty, int type)
            {
             
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

                
                this.type = type;
                this.difficulty = difficulty;
                
                if (type != LevelInterface.TYPE_OVERGROUND)
                {
                    odds[ODDS_HILL_STRAIGHT] = 0;
                }

                for (int i = 0; i < odds.length; i++)
                {
                    //failsafe (no negative odds)
                    if (odds[i] < 0){
                        odds[i] = 0;
                    }

                    totalOdds += odds[i];
                    odds[i] = totalOdds - odds[i];
                }

                lastSeed = seed;
                random = new Random(seed);

                //create the start location
                int length = 0;
                length += buildStraight(0, width, true);

                //create all of the medium sections
                while (length < width - 64)
                {
                    length += buildZone(length, width - length);
                }

                //set the end piece
                int floor = height - 1 - random.nextInt(4);

                xExit = length + 8;
                yExit = floor;

                // fills the end piece
                for (int x = length; x < width; x++)
                {
                    for (int y = 0; y < height; y++)
                    {
                        if (y >= floor)
                        {
                            setBlock(x, y, GROUND);
                        }
                    }
                }

                if (type == LevelInterface.TYPE_CASTLE || type == LevelInterface.TYPE_UNDERGROUND)
                {
                    int ceiling = 0;
                    int run = 0;
                    for (int x = 0; x < width; x++)
                    {
                        if (run-- <= 0 && x > 4)
                        {
                            ceiling = random.nextInt(4);
                            run = random.nextInt(4) + 4;
                        }
                        for (int y = 0; y < height; y++)
                        {
                            if ((x > 4 && y <= ceiling) || x < 1)
                            {
                                setBlock(x, y, GROUND);
                            }
                        }
                    }
                }

                fixWalls();

            }

            private int buildZone(int x, int maxLength)
            {
                int t = random.nextInt(totalOdds);
                int type = 0;

                for (int i = 0; i < odds.length; i++)
                {
                    if (odds[i] <= t)
                    {
                        type = i;
                    }
                }

                switch (type)
                {
                    case ODDS_STRAIGHT:
                        return buildStraight(x, maxLength, false);
                    case ODDS_HILL_STRAIGHT:
                        return buildHillStraight(x, maxLength);
                    case ODDS_TUBES:
                        return buildTubes(x, maxLength);
                    case ODDS_JUMPGAP:
                        if (gaps < Constraints.gaps)
                            return buildJump(x, maxLength);
                        else
                            return buildStraight(x, maxLength, false);
                    case ODDS_CANNONS:
                        return buildCannons(x, maxLength);
                }
                return 0;
            }

            private int buildJump(int xo, int maxLength)
            {    gaps++;
                //jl: jump length
                //js: the number of blocks that are available at either side for free
                int js = random.nextInt(4) + 2;
                int jl = random.nextInt(2) + 2;
                int length = js * 2 + jl;

                boolean hasStairs = random.nextInt(3) == 0;

                int floor = height - 1 - random.nextInt(4);
              //run from the start x position, for the whole length
                for (int x = xo; x < xo + length; x++)
                {
                    if (x < xo + js || x > xo + length - js - 1)
                    {
                        //run for all y's since we need to paint blocks upward
                        for (int y = 0; y < height; y++)
                        {    //paint ground up until the floor
                            if (y >= floor)
                            {
                                setBlock(x, y, GROUND);
                            }
                          //if it is above ground, start making stairs of rocks
                            else if (hasStairs)
                            {    //LEFT SIDE
                                if (x < xo + js)
                                { //we need to max it out and level because it wont
                                  //paint ground correctly unless two bricks are side by side
                                    if (y >= floor - (x - xo) + 1)
                                    {
                                        setBlock(x, y, ROCK);
                                    }
                                }
                                else
                                { //RIGHT SIDE
                                    if (y >= floor - ((xo + length) - x) + 2)
                                    {
                                        setBlock(x, y, ROCK);
                                    }
                                }
                            }
                        }
                    }
                }

                return length;
            }

            private int buildCannons(int xo, int maxLength)
            {
                int length = random.nextInt(10) + 2;
                if (length > maxLength) length = maxLength;

                int floor = height - 1 - random.nextInt(4);
                int xCannon = xo + 1 + random.nextInt(4);
                for (int x = xo; x < xo + length; x++)
                {
                    if (x > xCannon)
                    {
                        xCannon += 2 + random.nextInt(4);
                    }
                    if (xCannon == xo + length - 1) xCannon += 10;
                    int cannonHeight = floor - random.nextInt(4) - 1;

                    for (int y = 0; y < height; y++)
                    {
                        if (y >= floor)
                        {
                            setBlock(x, y, GROUND);
                        }
                        else
                        {
                            if (x == xCannon && y >= cannonHeight)
                            {
                                if (y == cannonHeight)
                                {
                                    setBlock(x, y, (byte) (14 + 0 * 16));
                                }
                                else if (y == cannonHeight + 1)
                                {
                                    setBlock(x, y, (byte) (14 + 1 * 16));
                                }
                                else
                                {
                                    setBlock(x, y, (byte) (14 + 2 * 16));
                                }
                            }
                        }
                    }
                }

                return length;
            }

            private int buildHillStraight(int xo, int maxLength)
            {
                int length = random.nextInt(10) + 10;
                if (length > maxLength) length = maxLength;

                int floor = height - 1 - random.nextInt(4);
                for (int x = xo; x < xo + length; x++)
                {
                    for (int y = 0; y < height; y++)
                    {
                        if (y >= floor)
                        {
                            setBlock(x, y, GROUND);
                        }
                    }
                }

                addEnemyLine(xo + 1, xo + length - 1, floor - 1);

                int h = floor;

                boolean keepGoing = true;

                boolean[] occupied = new boolean[length];
                while (keepGoing)
                {
                    h = h - 2 - random.nextInt(3);

                    if (h <= 0)
                    {
                        keepGoing = false;
                    }
                    else
                    {
                        int l = random.nextInt(5) + 3;
                        int xxo = random.nextInt(length - l - 2) + xo + 1;

                        if (occupied[xxo - xo] || occupied[xxo - xo + l] || occupied[xxo - xo - 1] || occupied[xxo - xo + l + 1])
                        {
                            keepGoing = false;
                        }
                        else
                        {
                            occupied[xxo - xo] = true;
                            occupied[xxo - xo + l] = true;
                            addEnemyLine(xxo, xxo + l, h - 1);
                            if (random.nextInt(4) == 0)
                            {
                                decorate(xxo - 1, xxo + l + 1, h);
                                keepGoing = false;
                            }
                            for (int x = xxo; x < xxo + l; x++)
                            {
                                for (int y = h; y < floor; y++)
                                {
                                    int xx = 5;
                                    if (x == xxo) xx = 4;
                                    if (x == xxo + l - 1) xx = 6;
                                    int yy = 9;
                                    if (y == h) yy = 8;

                                    if (getBlock(x, y) == 0)
                                    {
                                        setBlock(x, y, (byte) (xx + yy * 16));
                                    }
                                    else
                                    {
                                        if (getBlock(x, y) == HILL_TOP_LEFT) setBlock(x, y, HILL_TOP_LEFT_IN);
                                        if (getBlock(x, y) == HILL_TOP_RIGHT) setBlock(x, y, HILL_TOP_RIGHT_IN);
                                    }
                                }
                            }
                        }
                    }
                }

                return length;
            }

            private void addEnemyLine(int x0, int x1, int y)
            {
                for (int x = x0; x < x1; x++)
                {
                    if (random.nextInt(35) < difficulty + 1)
                    {
                        int type = random.nextInt(4);

                        if (difficulty < 1)
                        {
                            type = Enemy.ENEMY_GOOMBA;
                        }
                        else if (difficulty < 3)
                        {
                            type = random.nextInt(3);
                        }

                        setSpriteTemplate(x, y, new SpriteTemplate(type, random.nextInt(35) < difficulty));
                        ENEMIES++;
                    }
                }
            }

            private int buildTubes(int xo, int maxLength)
            {
                int length = random.nextInt(10) + 5;
                if (length > maxLength) length = maxLength;

                int floor = height - 1 - random.nextInt(4);
                int xTube = xo + 1 + random.nextInt(4);
                int tubeHeight = floor - random.nextInt(2) - 2;
                for (int x = xo; x < xo + length; x++)
                {
                    if (x > xTube + 1)
                    {
                        xTube += 3 + random.nextInt(4);
                        tubeHeight = floor - random.nextInt(2) - 2;
                    }
                    if (xTube >= xo + length - 2) xTube += 10;

                    if (x == xTube && random.nextInt(11) < difficulty + 1)
                    {
                        setSpriteTemplate(x, tubeHeight, new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
                        ENEMIES++;
                    }

                    for (int y = 0; y < height; y++)
                    {
                        if (y >= floor)
                        {
                            setBlock(x, y,GROUND);

                        }
                        else
                        {
                            if ((x == xTube || x == xTube + 1) && y >= tubeHeight)
                            {
                                int xPic = 10 + x - xTube;

                                if (y == tubeHeight)
                                {
                                    //tube top
                                    setBlock(x, y, (byte) (xPic + 0 * 16));
                                }
                                else
                                {
                                    //tube side
                                    setBlock(x, y, (byte) (xPic + 1 * 16));
                                }
                            }
                        }
                    }
                }

                return length;
            }

            private int buildStraight(int xo, int maxLength, boolean safe)
            {
                int length = random.nextInt(10) + 2;

                if (safe)
                    length = 10 + random.nextInt(5);

                if (length > maxLength)
                    length = maxLength;

                int floor = height - 1 - random.nextInt(4);

                //runs from the specified x position to the length of the segment
                for (int x = xo; x < xo + length; x++)
                {
                    for (int y = 0; y < height; y++)
                    {
                        if (y >= floor)
                        {
                            setBlock(x, y, GROUND);
                        }
                    }
                }

                if (!safe)
                {
                    if (length > 5)
                    {
                        decorate(xo, xo + length, floor);
                    }
                }

                return length;
            }

            private void decorate(int xStart, int xLength, int floor)
            {
                //if its at the very top, just return
                if (floor < 1)
                    return;

                //        boolean coins = random.nextInt(3) == 0;
                boolean rocks = true;

                //add an enemy line above the box
                addEnemyLine(xStart + 1, xLength - 1, floor - 1);

                int s = random.nextInt(4);
                int e = random.nextInt(4);

                if (floor - 2 > 0){
                    if ((xLength - 1 - e) - (xStart + 1 + s) > 1){
                        for(int x = xStart + 1 + s; x < xLength - 1 - e; x++){
                            setBlock(x, floor - 2, COIN);
                            COINS++;
                        }
                    }
                }

                s = random.nextInt(4);
                e = random.nextInt(4);
                
                //this fills the set of blocks and the hidden objects inside them
                if (floor - 4 > 0)
                {
                    if ((xLength - 1 - e) - (xStart + 1 + s) > 2)
                    {
                        for (int x = xStart + 1 + s; x < xLength - 1 - e; x++)
                        {
                            if (rocks)
                            {
                                if (x != xStart + 1 && x != xLength - 2 && random.nextInt(3) == 0)
                                {
                                    if (random.nextInt(4) == 0)
                                    {
                                        setBlock(x, floor - 4, BLOCK_POWERUP);
                                        BLOCKS_POWER++;
                                    }
                                    else
                                    {    //the fills a block with a hidden coin
                                        setBlock(x, floor - 4, BLOCK_COIN);
                                        BLOCKS_COINS++;
                                    }
                                }
                                else if (random.nextInt(4) == 0)
                                {
                                    if (random.nextInt(4) == 0)
                                    {
                                        setBlock(x, floor - 4, (byte) (2 + 1 * 16));
                                    }
                                    else
                                    {
                                        setBlock(x, floor - 4, (byte) (1 + 1 * 16));
                                    }
                                }
                                else
                                {
                                    setBlock(x, floor - 4, BLOCK_EMPTY);
                                    BLOCKS_EMPTY++;
                                }
                            }
                        }
                    }
                }
            }

            private void fixWalls()
            {
                boolean[][] blockMap = new boolean[width + 1][height + 1];

                for (int x = 0; x < width + 1; x++)
                {
                    for (int y = 0; y < height + 1; y++)
                    {
                        int blocks = 0;
                        for (int xx = x - 1; xx < x + 1; xx++)
                        {
                            for (int yy = y - 1; yy < y + 1; yy++)
                            {
                                if (getBlockCapped(xx, yy) == GROUND){
                                    blocks++;
                                }
                            }
                        }
                        blockMap[x][y] = blocks == 4;
                    }
                }
                blockify(this, blockMap, width + 1, height + 1);
            }

            private void blockify(Level level, boolean[][] blocks, int width, int height){
                int to = 0;
                if (type == LevelInterface.TYPE_CASTLE)
                {
                    to = 4 * 2;
                }
                else if (type == LevelInterface.TYPE_UNDERGROUND)
                {
                    to = 4 * 3;
                }

                boolean[][] b = new boolean[2][2];

                for (int x = 0; x < width; x++)
                {
                    for (int y = 0; y < height; y++)
                    {
                        for (int xx = x; xx <= x + 1; xx++)
                        {
                            for (int yy = y; yy <= y + 1; yy++)
                            {
                                int _xx = xx;
                                int _yy = yy;
                                if (_xx < 0) _xx = 0;
                                if (_yy < 0) _yy = 0;
                                if (_xx > width - 1) _xx = width - 1;
                                if (_yy > height - 1) _yy = height - 1;
                                b[xx - x][yy - y] = blocks[_xx][_yy];
                            }
                        }

                        if (b[0][0] == b[1][0] && b[0][1] == b[1][1])
                        {
                            if (b[0][0] == b[0][1])
                            {
                                if (b[0][0])
                                {
                                    level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                                }
                                else
                                {
                                    // KEEP OLD BLOCK!
                                }
                            }
                            else
                            {
                                if (b[0][0])
                                {
                                    //down grass top?
                                    level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                                }
                                else
                                {
                                    //up grass top
                                    level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                                }
                            }
                        }
                        else if (b[0][0] == b[0][1] && b[1][0] == b[1][1])
                        {
                            if (b[0][0])
                            {
                                //right grass top
                                level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                            }
                            else
                            {
                                //left grass top
                                level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                            }
                        }
                        else if (b[0][0] == b[1][1] && b[0][1] == b[1][0])
                        {
                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                        }
                        else if (b[0][0] == b[1][0])
                        {
                            if (b[0][0])
                            {
                                if (b[0][1])
                                {
                                    level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                                }
                                else
                                {
                                    level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                                }
                            }
                            else
                            {
                                if (b[0][1])
                                {
                                    //right up grass top
                                    level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                                }
                                else
                                {
                                    //left up grass top
                                    level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                                }
                            }
                        }
                        else if (b[0][1] == b[1][1])
                        {
                            if (b[0][1])
                            {
                                if (b[0][0])
                                {
                                    //left pocket grass
                                    level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                                }
                                else
                                {
                                    //right pocket grass
                                    level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                                }
                            }
                            else
                            {
                                if (b[0][0])
                                {
                                    level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                                }
                                else
                                {
                                    level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                                }
                            }
                        }
                        else
                        {
                            level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
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
                clone.BLOCKS_COINS = BLOCKS_COINS;
                clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
                clone.BLOCKS_POWER = BLOCKS_POWER;
                clone.ENEMIES = ENEMIES;
                clone.COINS = COINS;
                
                return clone;

              }
            
            public void setOdds() {
                odds[ODDS_STRAIGHT] = 20;
                odds[ODDS_HILL_STRAIGHT] = 10;
                odds[ODDS_TUBES] = 3;
                odds[ODDS_JUMPGAP] = 2;
                odds[ODDS_CANNONS] = 1;
                odds[ODDS_ENEMIES] = 10;
                odds[ODDS_COINS] = 10;
                odds[ODDS_COIN_BLOCK] = 10;
                odds[ODDS_BLOCK] = 10;
                
                String myName = myType.getName();
                //code for changing all the odds 
                if (myName.equals("Speeder")) {
                    odds[ODDS_JUMPGAP] = 10;
                    odds[ODDS_TUBES] = 0;
                }
                else if (myName.equals("Explorer")) {
                    odds[ODDS_HILL_STRAIGHT] = 20;
                    odds[ODDS_BLOCK] = 20;
                    odds[ODDS_COIN_BLOCK] = 20;
                }
                else if (myName.equals("Enemy Killer")) {
                    odds[ODDS_ENEMIES] = 30;
                }
                else if (myName.equals("Coin Collector")) {
                    odds[ODDS_COINS] = 30;
                    odds[ODDS_COIN_BLOCK] = 10;
                }
                else { //new player
                    odds[ODDS_ENEMIES] = 5;
                    odds[ODDS_JUMPGAP] = 0;
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