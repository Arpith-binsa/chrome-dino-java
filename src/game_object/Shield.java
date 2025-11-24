package game_object;

import static user_interface.GameScreen.GROUND_Y;
import static util.Resource.getImage;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import user_interface.GameScreen;

public class Shield {

    private GameScreen gameScreen;
    private List<ShieldItem> shields;
    private BufferedImage shieldImage;

    private static final int SHIELD_Y = GROUND_Y - 60; // Height where shield appears

    public Shield(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.shields = new ArrayList<>();
        this.shieldImage = getImage("resources/Shield.png");
    }

    public void updatePosition() {
        for (int i = 0; i < shields.size(); i++) {
            ShieldItem shield = shields.get(i);
            shield.x += gameScreen.getSpeedX();

            if (shield.x + shieldImage.getWidth() < 0) {
                shields.remove(i);
                i--;
            }
        }
    }

    public boolean spaceAvailable() {
        if (shields.isEmpty()) {
            return true;
        }
        ShieldItem lastShield = shields.get(shields.size() - 1);
        return lastShield.x < 0;
    }

    public boolean createShield() {
        if (spaceAvailable()) {
            shields.add(new ShieldItem(800)); // Start off-screen to the right
            return true;
        }
        return false;
    }

    public boolean checkCollection(Rectangle dinoHitbox) {
        for (int i = 0; i < shields.size(); i++) {
            ShieldItem shield = shields.get(i);
            if (!shield.collected && shield.getHitbox().intersects(dinoHitbox)) {
                shield.collected = true;
                shields.remove(i);
                return true;
            }
        }
        return false;
    }

    public void clearShields() {
        shields.clear();
    }

    public void draw(Graphics g) {
        for (ShieldItem shield : shields) {
            if (!shield.collected) {
                g.drawImage(shieldImage, (int)shield.x, SHIELD_Y, null);
            }
        }
    }

    public void drawHitbox(Graphics g) {
        for (ShieldItem shield : shields) {
            if (!shield.collected) {
                g.drawRect(shield.getHitbox().x, shield.getHitbox().y,
                        shield.getHitbox().width, shield.getHitbox().height);
            }
        }
    }

    private class ShieldItem {
        double x;
        boolean collected;

        ShieldItem(double x) {
            this.x = x;
            this.collected = false;
        }

        Rectangle getHitbox() {
            return new Rectangle((int)x, SHIELD_Y, shieldImage.getWidth(), shieldImage.getHeight());
        }
    }
}