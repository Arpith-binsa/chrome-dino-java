package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Resource {

    public static BufferedImage getImage(String path) {
        BufferedImage image = null;
        try {
            // Try loading from JAR first
            InputStream stream = Resource.class.getClassLoader().getResourceAsStream(path);
            if (stream != null) {
                image = ImageIO.read(stream);
                stream.close();
            } else {
                // Fallback to file system (for development)
                File file = new File(path);
                if (file.exists()) {
                    image = ImageIO.read(file);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
        }
        return image;
    }

    public static Clip getSound(String path) {
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();

            // Try loading from JAR first
            InputStream stream = Resource.class.getClassLoader().getResourceAsStream(path);
            if (stream != null) {
                clip.open(AudioSystem.getAudioInputStream(stream));
            } else {
                // Fallback to file system (for development)
                File file = new File(path);
                if (file.exists()) {
                    clip.open(AudioSystem.getAudioInputStream(file));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + path);
            e.printStackTrace();
        }
        return clip;
    }

    public static boolean isJar() {
        try {
            String protocol = Resource.class.getResource("Resource.class").getProtocol();
            return "jar".equals(protocol);
        } catch (Exception e) {
            return false;
        }
    }
}