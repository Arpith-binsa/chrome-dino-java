package manager;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BGMManager implements LineListener {
    
    private List<String> trackList;
    private List<String> currentPlaylist;
    private int currentTrackIndex;
    private Clip currentClip;
    private boolean isPlaying;
    
    public BGMManager() {
        trackList = new ArrayList<>();
        trackList.add("resources/Subwoofer-Lullaby.wav");
        trackList.add("resources/Biome-Fest.wav");
        trackList.add("resources/Watcher.wav");
        
        currentPlaylist = new ArrayList<>();
        currentTrackIndex = 0;
        isPlaying = false;
        
        shufflePlaylist();
    }
    
    private void shufflePlaylist() {
        currentPlaylist.clear();
        currentPlaylist.addAll(trackList);
        Collections.shuffle(currentPlaylist);
        currentTrackIndex = 0;
    }
    
    public void start() {
        if(!isPlaying) {
            playNextTrack();
            isPlaying = true;
        }
    }
    
    private void playNextTrack() {
        if(currentTrackIndex >= currentPlaylist.size()) {
            shufflePlaylist();
        }
        
        String trackPath = currentPlaylist.get(currentTrackIndex);
        currentTrackIndex++;
        
        try {
            if(currentClip != null) {
                currentClip.close();
            }
            
            File audioFile = new File(trackPath);
            
            if(!audioFile.exists()) {
                System.err.println("BGM file not found: " + trackPath);
                return;
            }
            
            System.out.println("Loading BGM: " + trackPath);
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            
            currentClip = (Clip) AudioSystem.getLine(info);
            currentClip.open(audioStream);
            
            // Set volume to 80%
            if(currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(0.8) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }
            
            currentClip.addLineListener(this);
            currentClip.start();
            
            System.out.println("Now playing: " + trackPath);
            
        } catch(Exception e) {
            System.err.println("Error playing BGM: " + trackPath);
            e.printStackTrace();
        }
    }
    
    @Override
    public void update(LineEvent event) {
        if(event.getType() == LineEvent.Type.STOP) {
            if(currentClip != null && currentClip.getFramePosition() >= currentClip.getFrameLength() - 1) {
                playNextTrack();
            }
        }
    }
    
    public void pause() {
        if(currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
    }
    
    public void resume() {
        if(currentClip != null && !currentClip.isRunning()) {
            currentClip.start();
        }
    }
    
    public void stop() {
        if(currentClip != null) {
            currentClip.stop();
            currentClip.close();
            isPlaying = false;
        }
    }
}
