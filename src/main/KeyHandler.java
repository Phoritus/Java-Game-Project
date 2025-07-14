package src.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    // KeyHandler implementation goes here
    // This class will handle key events for the game
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    GamePanel gp; // Reference to GamePanel

    @Override
    public void keyTyped(KeyEvent e) {
        // Handle key typed events
    }

    public KeyHandler(GamePanel gp) {
        this.gp = gp; // Store reference to GamePanel
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Handle key pressed events
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W) {
            // Move player up
            upPressed = true;
        } else if (keyCode == KeyEvent.VK_S) {
            // Move player down
            downPressed = true;
        } else if (keyCode == KeyEvent.VK_A) {
            // Move player left
            leftPressed = true;
        } else if (keyCode == KeyEvent.VK_D) {
            // Move player right
            rightPressed = true;
        } else if (keyCode == KeyEvent.VK_P) {
            // Toggle pause state
            System.out.println("P key pressed! Current state: " + gp.gameState);
            if (gp.gameState == gp.playState) {
                gp.gameState = gp.pauseState; // Pause the game
                System.out.println("Game paused!");
            } else if (gp.gameState == gp.pauseState) {
                gp.gameState = gp.playState; // Resume the game
                System.out.println("Game resumed!");
            }
        } else if (keyCode == KeyEvent.VK_M) {
            // Toggle music on/off
            gp.toggleMusic();
            System.out.println("Music toggled: " + (gp.musicOn ? "ON" : "OFF"));
        }

        // Debugging output
        if (keyCode == KeyEvent.VK_F1) {
            System.out.println("F1 pressed: Debugging mode activated");
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            System.out.println("Escape pressed: Exiting game");
            System.exit(0); // Exit the game on Escape key
        }

        

        
    }

    
    @Override
    public void keyReleased(KeyEvent e) {
        // Handle key released events
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_W) {
            upPressed = false;
        } else if (keyCode == KeyEvent.VK_S) {
            downPressed = false;
        } else if (keyCode == KeyEvent.VK_A) {
            leftPressed = false;
        } else if (keyCode == KeyEvent.VK_D) {
            rightPressed = false;
        }
    }
    
}
