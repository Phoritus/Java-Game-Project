package src.main;

import javax.sound.sampled.Clip;
import java.net.URL;
import javax.sound.sampled.AudioSystem;

public class Sound {
    Clip clip;
    URL soundURL[] = new URL[30];

    public Sound() {
        soundURL[0] = getClass().getResource("/res/sounds/SoundBG.wav");
        soundURL[1] = getClass().getResource("/res/sounds/keys.wav");
        soundURL[2] = getClass().getResource("/res/sounds/boots_speed.wav");
        soundURL[3] = getClass().getResource("/res/sounds/unlock.wav");
        soundURL[4] = getClass().getResource("/res/sounds/fanfare.wav");
        soundURL[5] = getClass().getResource("/res/sounds/hitSlime.wav");
        soundURL[6] = getClass().getResource("/res/sounds/swingSword.wav");
        soundURL[7] = getClass().getResource("/res/sounds/receivedDmg.wav");
        soundURL[8] = getClass().getResource("/res/sounds/levelUp.wav");
        soundURL[9] = getClass().getResource("/res/sounds/slideInven.wav");
    }

    public void setFile(int i) {
        try {
            clip = AudioSystem.getClip(); // Get a clip instance
            clip.open(AudioSystem.getAudioInputStream(soundURL[i]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.start(); // Start playing the sound
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the sound continuously
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop(); // Stop the sound
            clip.close(); // Close the clip to free resources
            clip = null; // Reset clip reference
        }
    }
}
