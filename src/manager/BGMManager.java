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
        trackList.add("resources/Subwoofer Lullaby.wav");
        trackList.add("resources/Biome Fest.wav");
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
        // If we've played all tracks, shuffle and start over
        if(currentTrackIndex >= currentPlaylist.size()) {
            shufflePlaylist();
        }
        
        String trackPath = currentPlaylist.get(currentTrackIndex);
        currentTrackIndex++;
        
        try {
            // Stop and close previous clip if exists
            if(currentClip != null) {
                currentClip.close();
            }
            
            // Load and play new track
            File audioFile = new File(trackPath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            currentClip.addLineListener(this);
            currentClip.start();
            
            System.out.println("Now playing: " + trackPath);
            
        } catch(Exception e) {
            System.err.println("Error playing BGM: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void update(LineEvent event) {
        // When track finishes, play next one
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
