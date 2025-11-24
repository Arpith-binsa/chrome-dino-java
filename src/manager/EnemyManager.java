package manager;

import game_object.Birds;
import game_object.Cactuses;
import game_object.Dino;
import game_object.Shield;
import misc.EnemyType;
import user_interface.GameScreen;

import java.awt.Graphics;
import java.awt.Rectangle;

public class EnemyManager {

    // value by which chance of creating new enemy increasing
    private static final double PERCENTAGE_INC = 0.0001;
    private static final double DISTANCE_DEC = -0.005;
    private static final int MINIMUM_DISTANCE = 250;

    private double distanceBetweenEnemies = 750;
    private double cactusesPercentage = 2;
    private double birdsPercentage = 1;
    private double shieldPercentage = 5.0; // Lower chance for shield

    private Cactuses cactuses;
    private Birds birds;
    private Shield shield;
    private GameScreen gameScreen;

    public EnemyManager(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        cactuses = new Cactuses(gameScreen, this);
        birds = new Birds(gameScreen, this);
        shield = new Shield(gameScreen);
    }

    public double getDistanceBetweenEnemies() {
        return distanceBetweenEnemies;
    }

    public double getCactusesPercentage() {
        return cactusesPercentage;
    }

    public double getBirdsPercentage() {
        return birdsPercentage;
    }

    public double getShieldPercentage() {
        return shieldPercentage;
    }

    public void updatePosition() {
        cactusesPercentage += PERCENTAGE_INC;
        birdsPercentage += PERCENTAGE_INC;
        shieldPercentage += PERCENTAGE_INC * 0.5; // Shield spawns less frequently
        if(distanceBetweenEnemies > MINIMUM_DISTANCE)
            distanceBetweenEnemies += DISTANCE_DEC;
        cactuses.updatePosition();
        birds.updatePosition();
        shield.updatePosition();

        if(cactuses.spaceAvailable() && birds.spaceAvailable() && shield.spaceAvailable()) {
            // "randomly" choosing new enemy type or shield
            EnemyType type = EnemyType.values()[(int)(Math.random() * EnemyType.values().length)];

            // Only spawn shield if score is above 50 (for testing, change to 400-500 later)
            if (type == EnemyType.SHIELD && gameScreen.getScore() < 50) {
                type = EnemyType.CACTUS; // Default to cactus if score too low
            }

            switch (type) {
                case SHIELD:
                    if(shield.createShield())
                        break;
                case CACTUS:
                    if(cactuses.createCactuses())
                        break;
                case BIRD:
                    if(birds.createBird())
                        break;
                default:
                    cactuses.createCactuses();
                    break;
            }
        }
    }

    public boolean checkShieldCollection(Dino dino) {
        return shield.checkCollection(dino.getHitbox());
    }

    public boolean isCollision(Rectangle hitBox) {
        if(cactuses.isCollision(hitBox) || birds.isCollision(hitBox))
            return true;
        return false;
    }

    public void clearEnemy() {
        cactuses.clearCactuses();
        birds.clearBirds();
        shield.clearShields();
    }

    public void draw(Graphics g) {
        cactuses.draw(g);
        birds.draw(g);
        shield.draw(g);
    }

    public void drawHitbox(Graphics g) {
        cactuses.drawHitbox(g);
        birds.drawHitbox(g);
        shield.drawHitbox(g);
    }

}