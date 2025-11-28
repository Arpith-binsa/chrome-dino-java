package user_interface;

import static user_interface.GameWindow.SCREEN_HEIGHT;
import static user_interface.GameWindow.SCREEN_WIDTH;
import static util.Resource.getImage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

import game_object.Clouds;
import game_object.Dino;
import game_object.Land;
import game_object.Rain;
import game_object.Score;
import manager.ControlsManager;
import manager.EnemyManager;
import manager.ShieldManager;
import manager.SoundManager;
import misc.Controls;
import misc.DinoState;
import misc.GameState;

@SuppressWarnings(value = { "serial" })
public class GameScreen extends JPanel implements Runnable {

    private Thread thread;

    private static final int STARTING_SPEED_X = -5;
    private static final double DIFFICULTY_INC = -0.0002;

    public static final double GRAVITY = 0.4;
    public static final int GROUND_Y = 280;
    public static final double SPEED_Y = -12;

    private final int FPS = 100;
    private final int NS_PER_FRAME = 1_000_000_000 / FPS;

    private double speedX = STARTING_SPEED_X;
    private GameState gameState = GameState.GAME_STATE_START;
    private int introCountdown = 1000;
    private boolean introJump = true;
    private boolean showHitboxes = false;
    private boolean collisions = true;
    
    // Invincibility frames after shield pops
    private int invincibilityFrames = 0;
    private static final int INVINCIBILITY_DURATION = 30; // 0.3 seconds at 100 FPS

    // Rain configuration
    private static final int RAIN_START_SCORE = 500;
    private static final int RAIN_DURATION = 3000; // 30 seconds (3000 frames at 100 FPS)
    private int rainTimer = 0;
    private boolean rainTriggered = false;

    private Controls controls;
    private Score score;
    private Dino dino;
    private Land land;
    private Clouds clouds;
    private Rain rain;
    private EnemyManager eManager;
    private ShieldManager sManager;
    private SoundManager gameOverSound;
    private ControlsManager cManager;

    public GameScreen() {
        thread = new Thread(this);
        controls = new Controls(this);
        super.add(controls.pressUp);
        super.add(controls.releaseUp);
        super.add(controls.pressDown);
        super.add(controls.releaseDown);
        super.add(controls.pressDebug);
        super.add(controls.pressPause);
        cManager = new ControlsManager(controls, this);
        score = new Score(this);
        dino = new Dino(controls);
        land = new Land(this);
        clouds = new Clouds(this);
        rain = new Rain(this);
        eManager = new EnemyManager(this);
        sManager = new ShieldManager(this, dino);
        gameOverSound = new SoundManager("resources/dead.wav");
        gameOverSound.startThread();
    }

    public void startThread() {
        thread.start();
    }

    @Override
    public void run() {
        long prevFrameTime = System.nanoTime();
        int waitingTime = 0;
        while(true) {
            cManager.update();
            updateFrame();
            repaint();
            waitingTime = (int)((NS_PER_FRAME - (System.nanoTime() - prevFrameTime)) / 1_000_000);
            if(waitingTime < 0)
                waitingTime = 1;
            SoundManager.WAITING_TIME = waitingTime;
            // little pause to not start new game if you are spamming your keys
            if(gameState == GameState.GAME_STATE_OVER)
                waitingTime = 1000;
            try {
                Thread.sleep(waitingTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            prevFrameTime = System.nanoTime();
        }
    }

    public double getSpeedX() {
        return speedX;
    }

    public GameState getGameState() {
        return gameState;
    }
    
    public int getScore() {
        return score.getCurrentScore();
    }

    // update all entities positions
    private void updateFrame() {
        switch (gameState) {
            case GAME_STATE_INTRO:
                dino.updatePosition();
                if(!introJump && dino.getDinoState() == DinoState.DINO_RUN)
                    land.updatePosition();
                clouds.updatePosition();
                introCountdown += speedX;
                if(introCountdown <= 0)
                    gameState = GameState.GAME_STATE_IN_PROGRESS;
                if(introJump) {
                    dino.jump();
                    dino.setDinoState(DinoState.DINO_JUMP);
                    introJump = false;
                }
                break;
            case GAME_STATE_IN_PROGRESS:
                speedX += DIFFICULTY_INC;
                dino.updatePosition();
                land.updatePosition();
                clouds.updatePosition();
                rain.updatePosition();
                eManager.updatePosition();
                sManager.updatePosition();
                
                // Decrease invincibility frames
                if(invincibilityFrames > 0) {
                    invincibilityFrames--;
                }

                // Rain logic - trigger at score 500, last for 30 seconds
                if(getScore() >= RAIN_START_SCORE && !rainTriggered) {
                    rain.setRaining(true);
                    rainTriggered = true;
                    rainTimer = RAIN_DURATION;
                }
                
                // Count down rain timer
                if(rainTriggered && rainTimer > 0) {
                    rainTimer--;
                    if(rainTimer == 0) {
                        rain.setRaining(false);
                    }
                }

                // Check for shield collection
                if(sManager.checkCollection(dino.getHitbox())) {
                    dino.activateShield();
                }

                // Check for collision only if not in invincibility frames
                if(collisions && invincibilityFrames == 0 && eManager.isCollision(dino.getHitbox())) {
                    if(dino.hasShield()) {
                        // Pop the shield and give brief invincibility
                        dino.popShield();
                        invincibilityFrames = INVINCIBILITY_DURATION;
                    } else {
                        // No shield - game over
                        gameState = GameState.GAME_STATE_OVER;
                        dino.dinoGameOver();
                        score.writeScore();
                        gameOverSound.play();
                    }
                }
                score.scoreUp();
                break;
            default:
                break;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(246, 246, 246));
        g.fillRect(0, 0, getWidth(), getHeight());
        switch (gameState) {
            case GAME_STATE_START:
                startScreen(g);
                break;
            case GAME_STATE_INTRO:
                introScreen(g);
                break;
            case GAME_STATE_IN_PROGRESS:
                inProgressScreen(g);
                break;
            case GAME_STATE_OVER:
                gameOverScreen(g);
                break;
            case GAME_STATE_PAUSED:
                pausedScreen(g);
                break;
            default:
                break;
        }
    }

    private void drawDebugMenu(Graphics g) {
        g.setColor(Color.RED);
        g.drawLine(0, GROUND_Y, getWidth(), GROUND_Y);
        dino.drawHitbox(g);
        eManager.drawHitbox(g);
        sManager.drawHitbox(g);
        String speedInfo = "SPEED_X: " + String.valueOf(Math.round(speedX * 1000D) / 1000D);
        g.drawString(speedInfo, (int)(SCREEN_WIDTH / 100), (int)(SCREEN_HEIGHT / 25));
        
        // Show invincibility frames in debug mode
        if(invincibilityFrames > 0) {
            g.setColor(Color.YELLOW);
            g.drawString("INVINCIBLE: " + invincibilityFrames, (int)(SCREEN_WIDTH / 100), (int)(SCREEN_HEIGHT / 25) + 20);
        }
        
        // Show rain timer in debug mode
        if(rain.isRaining()) {
            g.setColor(Color.CYAN);
            g.drawString("RAIN: " + (rainTimer / 100.0) + "s", (int)(SCREEN_WIDTH / 100), (int)(SCREEN_HEIGHT / 25) + 40);
        }
    }

    private void startScreen(Graphics g) {
        land.draw(g);
        dino.draw(g);
        BufferedImage introImage = getImage("resources/intro-text.png");
        Graphics2D g2d = (Graphics2D)g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, introCountdown / 1000f));
        g2d.drawImage(introImage, SCREEN_WIDTH / 2 - introImage.getWidth() / 2, SCREEN_HEIGHT / 2 - introImage.getHeight(), null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void introScreen(Graphics g) {
        clouds.draw(g);
        startScreen(g);
    }

    private void inProgressScreen(Graphics g) {
        clouds.draw(g);
        land.draw(g);
        sManager.draw(g); // Draw shields before enemies
        eManager.draw(g);
        dino.draw(g);
        score.draw(g);
        rain.draw(g); // Draw rain on top of everything
        rain.drawVignette(g); // Draw vignette effect last
        if(showHitboxes)
            drawDebugMenu(g);
    }

    private void gameOverScreen(Graphics g) {
        inProgressScreen(g);
        BufferedImage gameOverImage = getImage("resources/game-over.png");
        BufferedImage replayImage = getImage("resources/replay.png");
        g.drawImage(gameOverImage, SCREEN_WIDTH / 2 - gameOverImage.getWidth() / 2, SCREEN_HEIGHT / 2 - gameOverImage.getHeight() * 2, null);
        g.drawImage(replayImage, SCREEN_WIDTH / 2 - replayImage.getWidth() / 2, SCREEN_HEIGHT / 2, null);
    }

    private void pausedScreen(Graphics g) {
        inProgressScreen(g);
        BufferedImage pausedImage = getImage("resources/paused.png");
        g.drawImage(pausedImage, SCREEN_WIDTH / 2 - pausedImage.getWidth() / 2, SCREEN_HEIGHT / 2 - pausedImage.getHeight(), null);
    }

    public void pressUpAction() {
        if(gameState == GameState.GAME_STATE_IN_PROGRESS) {
            dino.jump();
            dino.setDinoState(DinoState.DINO_JUMP);
        }
    }

    public void releaseUpAction() {
        if(gameState == GameState.GAME_STATE_START)
            gameState = GameState.GAME_STATE_INTRO;
        if(gameState == GameState.GAME_STATE_OVER) {
            speedX = STARTING_SPEED_X;
            score.scoreReset();
            eManager.clearEnemy();
            sManager.clearShields();
            dino.resetDino();
            clouds.clearClouds();
            land.resetLand();
            rain.setRaining(false);
            rainTriggered = false;
            rainTimer = 0;
            invincibilityFrames = 0; // Reset invincibility
            gameState = GameState.GAME_STATE_IN_PROGRESS;
        }
    }

    public void pressDownAction() {
        if(dino.getDinoState() != DinoState.DINO_JUMP && gameState == GameState.GAME_STATE_IN_PROGRESS)
            dino.setDinoState(DinoState.DINO_DOWN_RUN);
    }

    public void releaseDownAction() {
        if(dino.getDinoState() != DinoState.DINO_JUMP && gameState == GameState.GAME_STATE_IN_PROGRESS)
            dino.setDinoState(DinoState.DINO_RUN);
    }

    public void pressDebugAction() {
        if(showHitboxes == false)
            showHitboxes = true;
        else
            showHitboxes = false;
        if(collisions == true)
            collisions = false;
        else
            collisions = true;
    }

    public void pressPauseAction() {
        if(gameState == GameState.GAME_STATE_IN_PROGRESS)
            gameState = GameState.GAME_STATE_PAUSED;
        else
            gameState = GameState.GAME_STATE_IN_PROGRESS;
    }

}
