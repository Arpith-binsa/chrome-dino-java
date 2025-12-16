package manager;

import game_object.Dino;
import game_object.Shield;
import user_interface.GameScreen;

import java.awt.Graphics;
import java.awt.Rectangle;

public class ShieldManager {

    // Shield spawn configuration
    private static final double SHIELD_SPAWN_PERCENTAGE = 0.5; // Low spawn rate for rarity
    private static final int MINIMUM_DISTANCE_BETWEEN_SHIELDS = 2000; // Shields spawn far apart
    private static final int MINIMUM_SCORE_FOR_SHIELD = 300; // Shields only appear after score 500

    private double distanceBetweenShields = MINIMUM_DISTANCE_BETWEEN_SHIELDS;
    private double shieldSpawnPercentage = SHIELD_SPAWN_PERCENTAGE;

    private Shield shield;
    private Dino dino;
    private GameScreen gameScreen;

    public ShieldManager(GameScreen gameScreen, Dino dino) {
        shield = new Shield(gameScreen, this);
        this.dino = dino;
        this.gameScreen = gameScreen;
    }

    public double getDistanceBetweenShields() {
        return distanceBetweenShields;
    }

    public double getShieldSpawnPercentage() {
        return shieldSpawnPercentage;
    }

    public void updatePosition() {
        shield.updatePosition();
        // Only spawn shield if:
        // 1. Space is available
        // 2. Player doesn't have a shield
        // 3. Score is at least 500
        if(shield.spaceAvailable() && !dino.hasShield() && gameScreen.getScore() >= MINIMUM_SCORE_FOR_SHIELD) {
            shield.createShield();
        }
    }

    public boolean checkCollection(Rectangle dinoHitBox) {
        return shield.checkCollection(dinoHitBox);
    }

    public void clearShields() {
        shield.clearShields();
    }

    public void draw(Graphics g) {
        shield.draw(g);
    }

    public void drawHitbox(Graphics g) {
        shield.drawHitbox(g);
    }

}
