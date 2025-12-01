package game_object;

import static user_interface.GameScreen.GROUND_Y;
import static user_interface.GameWindow.SCREEN_WIDTH;
import static util.Resource.getImage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import misc.Animation;
import user_interface.GameScreen;

public class Boss {
    
    private class BossBird {
        private static final int BOSS_SIZE_MULTIPLIER = 3; // 3x normal bird size
        private static final int NORMAL_BIRD_Y = 150; // Starting height
        private static final int SWOOP_Y = GROUND_Y - 100; // How low it swoops
        private static final int SWOOP_START_X = 400; // When to start swooping (distance from dino)
        private static final int SWOOP_DURATION = 60; // Frames for swoop animation
        
        private double x;
        private double y;
        private double targetY;
        private int swoopProgress;
        private boolean isSwooping;
        private boolean hasSwooped;
        private Animation bossFly;
        
        private BossBird(double x, Animation bossFly) {
            this.x = x;
            this.y = NORMAL_BIRD_Y;
            this.targetY = NORMAL_BIRD_Y;
            this.bossFly = bossFly;
            this.swoopProgress = 0;
            this.isSwooping = false;
            this.hasSwooped = false;
        }
        
        private void updatePosition(double speedX) {
            x += speedX;
            bossFly.updateSprite();
            
            // Check if boss should start swooping
            if(!hasSwooped && x <= SWOOP_START_X) {
                isSwooping = true;
                hasSwooped = true;
            }
            
            // Handle swoop animation
            if(isSwooping) {
                swoopProgress++;
                
                // Swoop down (first half)
                if(swoopProgress < SWOOP_DURATION / 2) {
                    double swoopPercent = (double)swoopProgress / (SWOOP_DURATION / 2);
                    y = NORMAL_BIRD_Y + (SWOOP_Y - NORMAL_BIRD_Y) * swoopPercent;
                }
                // Swoop back up (second half)
                else if(swoopProgress < SWOOP_DURATION) {
                    double swoopPercent = (double)(swoopProgress - SWOOP_DURATION / 2) / (SWOOP_DURATION / 2);
                    y = SWOOP_Y + (NORMAL_BIRD_Y - SWOOP_Y) * swoopPercent;
                }
                else {
                    isSwooping = false;
                    y = NORMAL_BIRD_Y;
                }
            }
        }
        
        private Rectangle getHitbox() {
            int width = bossFly.getSprite().getWidth() * BOSS_SIZE_MULTIPLIER;
            int height = bossFly.getSprite().getHeight() * BOSS_SIZE_MULTIPLIER;
            return new Rectangle((int)x, (int)y, width, height);
        }
        
        private void draw(Graphics g) {
            int width = bossFly.getSprite().getWidth() * BOSS_SIZE_MULTIPLIER;
            int height = bossFly.getSprite().getHeight() * BOSS_SIZE_MULTIPLIER;
            g.drawImage(bossFly.getSprite(), (int)x, (int)y, width, height, null);
        }
        
        private void drawHitbox(Graphics g) {
            g.setColor(Color.RED);
            g.drawRect(getHitbox().x, getHitbox().y, getHitbox().width, getHitbox().height);
        }
        
        private boolean isOutOfScreen() {
            return x < -bossFly.getSprite().getWidth() * BOSS_SIZE_MULTIPLIER;
        }
    }
    
    private BossBird boss;
    private GameScreen gameScreen;
    private Animation bossFly;
    private manager.SoundManager bossSound;
    
    public Boss(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        bossFly = new Animation(150);
        bossFly.addSprite(getImage("resources/bird-fly-1.png"));
        bossFly.addSprite(getImage("resources/bird-fly-2.png"));
        bossSound = new manager.SoundManager("resources/boss-wings.wav"); // Your flapping sound
        bossSound.startThread();
    }
    
    public void spawnBoss() {
        boss = new BossBird(SCREEN_WIDTH, bossFly);
        bossSound.play(); // Play wing flapping sound
    }
    
    public void updatePosition() {
        if(boss != null) {
            boss.updatePosition(gameScreen.getSpeedX());
            
            // Remove boss if it's off screen
            if(boss.isOutOfScreen()) {
                boss = null;
            }
        }
    }
    
    public boolean isCollision(Rectangle dinoHitBox) {
        if(boss != null) {
            return boss.getHitbox().intersects(dinoHitBox);
        }
        return false;
    }
    
    public boolean isActive() {
        return boss != null;
    }
    
    public void clearBoss() {
        boss = null;
    }
    
    public void draw(Graphics g) {
        if(boss != null) {
            boss.draw(g);
        }
    }
    
    public void drawHitbox(Graphics g) {
        if(boss != null) {
            boss.drawHitbox(g);
        }
    }
}
