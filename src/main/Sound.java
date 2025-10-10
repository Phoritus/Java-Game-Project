package src.main;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.net.URL;
import javax.sound.sampled.AudioSystem;

public class Sound {
    Clip clip;
    URL soundURL[] = new URL[30];
    FloatControl fc;
    int volumeScale = 3;
    float volume;

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
        soundURL[10] = getClass().getResource("/res/sounds/fireball.wav");
        soundURL[11] = getClass().getResource("/res/sounds/cuttree.wav");
        soundURL[12] = getClass().getResource("/res/sounds/gameover.wav");
        soundURL[13] = getClass().getResource("/res/sounds/tele1.wav");
        soundURL[14] = getClass().getResource("/res/sounds/tele2.wav");
        soundURL[15] = getClass().getResource("/res/sounds/sleepEffect.wav");
        soundURL[16] = getClass().getResource("/res/sounds/fisrt_dungeon.wav");
        soundURL[17] = getClass().getResource("/res/sounds/boss_dungeon.wav");
        soundURL[18] = getClass().getResource("/res/sounds/mystery_dungeon.wav");
    }

    public void setFile(int i) {
        try {
            clip = AudioSystem.getClip(); // Get a clip instance
            clip.open(AudioSystem.getAudioInputStream(soundURL[i]));
            fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            checkVolume();
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

    public void checkVolume() {
        switch (volumeScale) {
            case 0: volume = -80f; break;
            case 1: volume = -20f; break;
            case 2: volume = -12f; break;
            case 3: volume = -5f; break;
            case 4: volume = 1f; break;
            case 5: volume = 6f; break;
        }
        fc.setValue(volume);
    }
}
