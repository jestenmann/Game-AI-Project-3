package dk.itu.mario.level.events;

import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.level.Level;

public class EnemyLineEvent extends Event {
	private int difficulty = 0;
	private SpriteTemplate[] templates;
	public EnemyLineEvent(int x, int dy, int length, int difficulty, Level source) {
		super(x, dy, length);
		this.difficulty = difficulty;
		this.priority = 10;
		generate();
	}

	@Override
	protected void generate() {
		int height = 1;
		this.grid = new byte[height][length];
		this.templates = new SpriteTemplate[length];
		// Lovingly adapted from RandomLevel
        for (int x = 0; x < this.length; x++)
        {
            if (rand.nextInt(15) < difficulty + 1)
            {
                int type = rand.nextInt(4);

                if (difficulty < 1)
                {
                    type = Enemy.ENEMY_GOOMBA;
                }
                else if (difficulty < 3)
                {
                    type = rand.nextInt(3);
                }

                templates[x] = new SpriteTemplate(type, rand.nextInt(35) < difficulty);
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

	public SpriteTemplate[] getTemplates() {
		return templates;
	}

	public void setTemplates(SpriteTemplate[] templates) {
		this.templates = templates;
	}
}
