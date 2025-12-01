package manager;
import static util.Resource.getSound;
import javax.sound.sampled.Clip;

// creating new thread for every sound because when i tried to just play sound in main thread
// i had laggy sound and sometimes and delayed a lot so.....
public class SoundManager implements Runnable {
        public static int WAITING_TIME = 0;
        
        Thread thread;
        
        private boolean playSound = false;
        private String path;
        private Clip clip;
        
        public SoundManager(String path) {
                thread = new Thread(this);
                this.path = path;
        }
        
        @Override
        public void run() {
                // constantly running with same delays i calculate in GameScreen
                // playSound sets to true if i want to play sound
                while(true) {
                        try {
                                Thread.sleep(WAITING_TIME);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                        if(playSound) {
//                              System.out.println("played " + path);
                                clip = getSound(path);
                                clip.start();
                                playSound = false;
                        }
                }
        }
        public void startThread() {
                thread.start();
        }
        
        public void play() {
                playSound = true;
        }
        
        public void pause() {
                if(clip != null && clip.isRunning()) {
                        clip.stop();
                }
        }
        
        public void resume() {
                if(clip != null && !clip.isRunning()) {
                        clip.start();
                }
        }
        
        public void stop() {
                if(clip != null) {
                        clip.stop();
                        clip.close();
                        clip = null;
                }
        }
        
}
