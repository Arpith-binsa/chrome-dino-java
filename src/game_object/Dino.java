package game_object;

import static user_interface.GameScreen.GRAVITY;
import static user_interface.GameScreen.GROUND_Y;
import static user_interface.GameScreen.SPEED_Y;
import static util.Resource.getImage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import manager.SoundManager;
import misc.Animation;
import misc.Controls;
import misc.DinoState;

public class Dino {

    // values to subtract from x, y, width, height to get accurate hitbox
    private static final int[] HITBOX_RUN = {12, 26, -32, -42};
    private static final int[] HITBOX_DOWN_RUN = {24, 8, -60, -24};

    public static final double X = 120;

    // Shield configuration
    private static final int SHIELD_DURATION = 10000; // 10 seconds of invincibility
    private static final int SHIELD_BLINK_START = 8000; // Start blinking at 8 seconds (2 sec before end)
    private static final int BLINK_INTERVAL = 100; // Blink every 100ms (Mario-style fast blinking)

    Controls controls;

    private double maxY;
    private double highJumpMaxY;
    private double lowJumpMaxY;

    private double y = 0;
    private double speedY = 0;

    private DinoState dinoState;
    private BufferedImage dinoJump;
    private BufferedImage dinoDead;
    private BufferedImage shieldIcon;
    private Animation dinoRun;
    private Animation dinoDownRun;
    private SoundManager jumpSound;
    private SoundManager shieldEquipSound;
    private SoundManager shieldPopSound;

    // Shield state variables
    private boolean hasShield = false;
    private long shieldStartTime = 0;
    private boolean shieldVisible = true; // For blinking effect

    public Dino(Controls controls) {
        this.controls = controls;
        dinoRun = new Animation(150);
        dinoRun.addSprite(getImage("resources/dino-run-1.png"));
        dinoRun.addSprite(getImage("resources/dino-run-2.png"));
        dinoDownRun = new Animation(150);
        dinoDownRun.addSprite(getImage("resources/dino-down-run-1.png"));
        dinoDownRun.addSprite(getImage("resources/dino-down-run-2.png"));
        dinoJump = getImage("resources/dino-jump.png");
        dinoDead = getImage("resources/dino-dead.png");
        shieldIcon = getImage("resources/Shield.png");
        jumpSound = new SoundManager("resources/jump.wav");
        jumpSound.startThread();
        shieldEquipSound = new SoundManager("resources/BubbleEquip.wav");
        shieldEquipSound.startThread();
        shieldPopSound = new SoundManager("resources/BubblePop.wav");
        shieldPopSound.startThread();
        y = GROUND_Y - dinoJump.getHeight();
        maxY = y;
        highJumpMaxY = setJumpMaxY(GRAVITY);
        lowJumpMaxY = setJumpMaxY(GRAVITY + GRAVITY / 2);
        dinoState = DinoState.DINO_JUMP;
    }

    public DinoState getDinoState() {
        return dinoState;
    }

    public void setDinoState(DinoState dinoState) {
        this.dinoState = dinoState;
    }

    public boolean hasShield() {
        return hasShield;
    }

    public void activateShield() {
        hasShield = true;
        shieldStartTime = System.currentTimeMillis();
        shieldVisible = true;
        shieldEquipSound.play(); // Play equip sound
    }

    public void deactivateShield() {
        hasShield = false;
        shieldVisible = true;
    }
    
    public void popShield() {
        if(hasShield) {
            hasShield = false;
            shieldVisible = true;
            shieldPopSound.play(); // Play pop sound
        }
    }

    private void updateShield() {
        if(hasShield) {
            long elapsedTime = System.currentTimeMillis() - shieldStartTime;

            // Check if shield should expire
            if(elapsedTime >= SHIELD_DURATION) {
                popShield(); // Pop sound when time runs out
            }
            // Start blinking effect when shield is about to expire
            else if(elapsedTime >= SHIELD_BLINK_START) {
                // Toggle visibility for blinking effect
                shieldVisible = (elapsedTime / BLINK_INTERVAL) % 2 == 0;
            }
            else {
                shieldVisible = true;
            }
        }
    }

    public double setJumpMaxY(double gravity) {
        speedY = SPEED_Y;
        y += speedY;
        double jumpMaxY = y;
        while(true) {
            speedY += gravity;
            y += speedY;
            if(y < jumpMaxY)
                jumpMaxY = y;
            if(y + speedY >= GROUND_Y - dinoRun.getSprite().getHeight()) {
                speedY = 0;
                y = GROUND_Y - dinoRun.getSprite().getHeight();
                break;
            }
        }
        return jumpMaxY;
    }

    public Rectangle getHitbox() {
        switch (dinoState) {
            case DINO_RUN:
            case DINO_JUMP:
            case DINO_DEAD:
                return new Rectangle((int)X + HITBOX_RUN[0], (int)y + HITBOX_RUN[1],
                        dinoDead.getWidth() + HITBOX_RUN[2], dinoDead.getHeight() + HITBOX_RUN[3]);
            case DINO_DOWN_RUN:
                return new Rectangle((int)X + HITBOX_DOWN_RUN[0], (int)y + HITBOX_DOWN_RUN[1],
                        dinoDownRun.getSprite().getWidth() + HITBOX_DOWN_RUN[2], dinoDownRun.getSprite().getHeight() + HITBOX_DOWN_RUN[3]);
        }
        return null;
    }

    public void updatePosition() {
        if(y < maxY)
            maxY = y;
        dinoRun.updateSprite();
        dinoDownRun.updateSprite();
        updateShield(); // Update shield state

        switch (dinoState) {
            case DINO_RUN:
                y = GROUND_Y - dinoRun.getSprite().getHeight();
                maxY = y;
                break;
            case DINO_DOWN_RUN:
                y = GROUND_Y - dinoDownRun.getSprite().getHeight();
                break;
            case DINO_JUMP:
                if(y + speedY >= GROUND_Y - dinoRun.getSprite().getHeight()) {
                    speedY = 0;
                    y = GROUND_Y - dinoRun.getSprite().getHeight();
                    dinoState = DinoState.DINO_RUN;
                } else if(controls.isPressedUp()) {
                    speedY += GRAVITY;
                    y += speedY;
                } else {
                    if(maxY <= lowJumpMaxY - (lowJumpMaxY - highJumpMaxY) / 2)
                        speedY += GRAVITY;
                    else
                        speedY += GRAVITY + GRAVITY / 2;
                    if(controls.isPressedDown())
                        speedY += GRAVITY;
                    y += speedY;
                }
                break;
            default:
                break;
        }

    }

    public void jump() {
        if(y == GROUND_Y - dinoRun.getSprite().getHeight()) {
            jumpSound.play();
            speedY = SPEED_Y;
            y += speedY;
        }
    }

    public void resetDino() {
        y = GROUND_Y - dinoJump.getHeight();
        dinoState = DinoState.DINO_RUN;
        deactivateShield(); // Remove shield on reset
    }

    public void dinoGameOver() {
        if(y > GROUND_Y - dinoDead.getHeight())
            y = GROUND_Y - dinoDead.getHeight();
        dinoState = DinoState.DINO_DEAD;
        deactivateShield(); // Remove shield on game over
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        // Apply blinking effect if shield is not visible
        if(hasShield && !shieldVisible) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }

        // Draw the dino
        switch (dinoState) {
            case DINO_RUN:
                g2d.drawImage(dinoRun.getSprite(), (int)X, (int)y, null);
                break;
            case DINO_DOWN_RUN:
                g2d.drawImage(dinoDownRun.getSprite(), (int)X, (int)y, null);
                break;
            case DINO_JUMP:
                g2d.drawImage(dinoJump, (int)X, (int)y, null);
                break;
            case DINO_DEAD:
                g2d.drawImage(dinoDead, (int)X, (int)y, null);
                break;
            default:
                break;
        }

        // Reset composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Draw shield icon centered on dino if shield is active and visible
        if(hasShield && shieldVisible) {
            // Get dino dimensions
            int dinoWidth = dinoState == DinoState.DINO_DOWN_RUN ? 
                    dinoDownRun.getSprite().getWidth() : dinoRun.getSprite().getWidth();
            int dinoHeight = dinoState == DinoState.DINO_DOWN_RUN ? 
                    dinoDownRun.getSprite().getHeight() : dinoRun.getSprite().getHeight();
            
            // Scale shield to 210% of dino size
            int shieldWidth = (int)(dinoWidth * 2.1);
            int shieldHeight = (int)(dinoHeight * 2.1);
            
            // Center shield on dino
            int shieldX = (int)X + (dinoWidth / 2) - (shieldWidth / 2);
            int shieldY = (int)y + (dinoHeight / 2) - (shieldHeight / 2);

            // Add a slight pulsing effect to the shield icon
            long elapsedTime = System.currentTimeMillis() - shieldStartTime;
            float pulseAlpha = 0.6f + (float)(Math.sin(elapsedTime * 0.005) * 0.2);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha));
            g2d.drawImage(shieldIcon, shieldX, shieldY, shieldWidth, shieldHeight, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    public void drawHitbox(Graphics g) {
        g.setColor(Color.GREEN);
        g.drawRect(getHitbox().x, getHitbox().y, getHitbox().width, getHitbox().height);

        // Draw shield status in debug mode
        if(hasShield) {
            g.setColor(Color.CYAN);
            long remainingTime = SHIELD_DURATION - (System.currentTimeMillis() - shieldStartTime);
            g.drawString("SHIELD: " + (remainingTime / 1000.0) + "s", (int)X, (int)y - 10);
        }
    }

}
