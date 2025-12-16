package game_object;

import static user_interface.GameScreen.GROUND_Y;
import static user_interface.GameWindow.SCREEN_WIDTH;
import static util.Resource.getImage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import manager.ShieldManager;
import user_interface.GameScreen;

public class Shield {

    private class ShieldPowerUp {

        private BufferedImage shieldImage;
        private double x;
        private int y;
        private long spawnTime;

        private ShieldPowerUp(BufferedImage shieldImage, double x, int y) {
            this.shieldImage = shieldImage;
            this.x = x;
            this.y = y;
            this.spawnTime = System.currentTimeMillis();
        }

    }

    // Hitbox adjustment for shield
    private static final int HITBOX_X_OFFSET = 5;
    private static final int HITBOX_Y_OFFSET = 5;
    private static final int HITBOX_WIDTH_REDUCTION = 10;
    private static final int HITBOX_HEIGHT_REDUCTION = 10;

    // Shield will hover at this height above ground
    private static final int HOVER_HEIGHT = 0;
    // Slight bobbing animation range (in pixels)
    private static final int BOB_RANGE = 5;
    // Bobbing animation speed
    private static final double BOB_SPEED = 0.003;

    private ShieldManager sManager;
    private GameScreen gameScreen;
    private List<ShieldPowerUp> shields;
    private BufferedImage shieldImage;

    public Shield(GameScreen gameScreen, ShieldManager sManager) {
        this.sManager = sManager;
        this.gameScreen = gameScreen;
        shields = new ArrayList<ShieldPowerUp>();
        shieldImage = getImage("resources/Shield.png");
    }

    public void updatePosition() {
        for(Iterator<ShieldPowerUp> i = shields.iterator(); i.hasNext();) {
            ShieldPowerUp shield = i.next();
            shield.x += gameScreen.getSpeedX();
            // When the Shield isnt collected and it goes off screen sometime it creates bugs, so here those shields are removed
            if((int)shield.x + shield.shieldImage.getWidth() < 0) {
                i.remove();
            }
        }
    }

    public boolean spaceAvailable() {
        for(Iterator<ShieldPowerUp> i = shields.iterator(); i.hasNext();) {
            ShieldPowerUp shield = i.next();
            if(SCREEN_WIDTH - (shield.x + shield.shieldImage.getWidth() / 4) < sManager.getDistanceBetweenShields()) {
                return false;
            }
        }
        return true;
    }

    public boolean createShield() {
        if(Math.random() * 100 < sManager.getShieldSpawnPercentage()) {
            int scaledHeight = shieldImage.getHeight() / 4;
            int yPos = GROUND_Y - HOVER_HEIGHT - scaledHeight;
            shields.add(new ShieldPowerUp(shieldImage, SCREEN_WIDTH, yPos));
            return true;
        }
        return false;
    }

    public boolean checkCollection(Rectangle dinoHitBox) {
        for(Iterator<ShieldPowerUp> i = shields.iterator(); i.hasNext();) {
            ShieldPowerUp shield = i.next();
            Rectangle shieldHitBox = getHitbox(shield);
            if(shieldHitBox.intersects(dinoHitBox)) {
                i.remove();
                return true;
            }
        }
        return false;
    }

    private Rectangle getHitbox(ShieldPowerUp shield) {
        // Calculate bobbing offset based on time
        long timeSinceSpawn = System.currentTimeMillis() - shield.spawnTime;
        int bobOffset = (int)(Math.sin(timeSinceSpawn * BOB_SPEED) * BOB_RANGE);
        
        int scaledWidth = shield.shieldImage.getWidth() / 4;
        int scaledHeight = shield.shieldImage.getHeight() / 4;

        return new Rectangle(
                (int)shield.x + HITBOX_X_OFFSET,
                shield.y + bobOffset + HITBOX_Y_OFFSET,
                scaledWidth - HITBOX_WIDTH_REDUCTION,
                scaledHeight - HITBOX_HEIGHT_REDUCTION
        );
    }

    public void clearShields() {
        shields.clear();
    }

    public void draw(Graphics g) {
        for(Iterator<ShieldPowerUp> i = shields.iterator(); i.hasNext();) {
            ShieldPowerUp shield = i.next();
            // Calculate bobbing animation
            long timeSinceSpawn = System.currentTimeMillis() - shield.spawnTime;
            int bobOffset = (int)(Math.sin(timeSinceSpawn * BOB_SPEED) * BOB_RANGE);

            // Draw shield at 25% size (much smaller, similar to cactus)
            int scaledWidth = shield.shieldImage.getWidth() / 4;
            int scaledHeight = shield.shieldImage.getHeight() / 4;
            g.drawImage(shield.shieldImage, (int)shield.x, shield.y + bobOffset, scaledWidth, scaledHeight, null);
        }
    }

    public void drawHitbox(Graphics g) {
        g.setColor(Color.CYAN);
        for(Iterator<ShieldPowerUp> i = shields.iterator(); i.hasNext();) {
            ShieldPowerUp shield = i.next();
            Rectangle shieldHitBox = getHitbox(shield);
            g.drawRect(shieldHitBox.x, shieldHitBox.y, (int)shieldHitBox.getWidth(), (int)shieldHitBox.getHeight());
        }
    }

}
