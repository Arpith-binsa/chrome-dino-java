package game_object;

import user_interface.GameScreen;
import manager.SoundManager;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static user_interface.GameWindow.SCREEN_WIDTH;
import static user_interface.GameWindow.SCREEN_HEIGHT;

public class Rain {
    
    private class RainDrop {
        double x;
        double y;
        double speed;
        int width;
        int height;
        
        public RainDrop(double x, double y, double speed, int width, int height) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.width = width;
            this.height = height;
        }
    }
    
    private static final int RAIN_DROP_COUNT = 100;
    private static final int MIN_SPEED = 18;
    private static final int MAX_SPEED = 28;
    private static final int MIN_WIDTH = 3;
    private static final int MAX_WIDTH = 6;
    private static final int MIN_HEIGHT = 15;
    private static final int MAX_HEIGHT = 25;
    
    private GameScreen gameScreen;
    private List<RainDrop> rainDrops;
    private Random random;
    private boolean isRaining;
    private SoundManager rainSound;
    
    public Rain(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        rainDrops = new ArrayList<RainDrop>();
        random = new Random();
        isRaining = false;
        rainSound = new SoundManager("resources/Rain.wav");
        rainSound.startThread();
        initRain();
    }
    
    private void initRain() {
        rainDrops.clear();
        for(int i = 0; i < RAIN_DROP_COUNT; i++) {
            double x = random.nextDouble() * SCREEN_WIDTH;
            double y = random.nextDouble() * SCREEN_HEIGHT;
            double speed = MIN_SPEED + random.nextDouble() * (MAX_SPEED - MIN_SPEED);
            int width = MIN_WIDTH + random.nextInt(MAX_WIDTH - MIN_WIDTH);
            int height = MIN_HEIGHT + random.nextInt(MAX_HEIGHT - MIN_HEIGHT);
            rainDrops.add(new RainDrop(x, y, speed, width, height));
        }
    }
    
    public void setRaining(boolean raining) {
        this.isRaining = raining;
        if(raining && rainDrops.isEmpty()) {
            initRain();
        }
        
        if(raining) {
            rainSound.play();
        }
    }
    
    public boolean isRaining() {
        return isRaining;
    }
    
    public void pause() {
        rainSound.pause();
    }
    
    public void resume() {
        rainSound.resume();
    }
    
    public void updatePosition() {
        if(!isRaining) return;
        
        for(Iterator<RainDrop> i = rainDrops.iterator(); i.hasNext();) {
            RainDrop drop = i.next();
            drop.y += drop.speed;
            drop.x += gameScreen.getSpeedX() / 2;
            
            if(drop.y > SCREEN_HEIGHT) {
                drop.y = -drop.height;
                drop.x = random.nextDouble() * SCREEN_WIDTH;
            }
            
            if(drop.x < -10) {
                drop.x = SCREEN_WIDTH + 10;
            }
        }
    }
    
    public void draw(Graphics g) {
        if(!isRaining) return;
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        
        for(RainDrop drop : rainDrops) {
            g2d.setColor(new Color(40, 40, 40));
            g2d.fillRect((int)drop.x, (int)drop.y, drop.width, drop.height);
            g2d.setColor(new Color(80, 80, 80));
            g2d.fillRect((int)drop.x, (int)drop.y, 1, drop.height);
        }
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
    
    public void drawVignette(Graphics g) {
        if(!isRaining) return;
        
        Graphics2D g2d = (Graphics2D)g;
        
        Point2D center = new Point2D.Float(200, SCREEN_HEIGHT / 2f);
        float radius = 440f;
        
        float[] dist = {0.0f, 0.3f, 0.6f, 0.85f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 40),
            new Color(0, 0, 0, 120),
            new Color(0, 0, 0, 220),
            new Color(0, 0, 0, 255)
        };
        
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g2d.setPaint(null);
    }
}
