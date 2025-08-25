package src.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    // KeyHandler implementation goes here
    // This class will handle key events for the game
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, fPressed, tPressed;
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

        // Title State
        if (gp.gameState == gp.titleState) {
            if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
                gp.ui.commandNumber--; // Move up in the menu
                if (gp.ui.commandNumber < 0) {
                    gp.ui.commandNumber = 2; // Wrap to last option (QUIT GAME)
                }
                gp.repaint(); // Force repaint to update UI
            } else if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
                gp.ui.commandNumber++; // Move down in the menu
                if (gp.ui.commandNumber > 2) {
                    gp.ui.commandNumber = 0; // Wrap to first option (NEW GAME)
                }
                gp.repaint(); // Force repaint to update UI
            } else if (keyCode == KeyEvent.VK_ENTER) {
                // Handle menu selection
                if (gp.ui.commandNumber == 0) {
                    // NEW GAME
                    gp.gameState = gp.playState;
                } else if (gp.ui.commandNumber == 1) {
                    // LOAD GAME (not implemented yet)
                    gp.gameState = gp.playState; // For now, just start the game
                } else if (gp.ui.commandNumber == 2) {
                    // QUIT GAME
                    System.exit(0);
                }
            }
        }

    if (gp.gameState == gp.playState || gp.gameState == gp.pauseState) {
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
                gp.gameState = (gp.gameState == gp.playState) ? gp.pauseState : gp.playState; // Toggle between play and pause
            } else if (keyCode == KeyEvent.VK_F) {
                // Handle F key for healing and other actions
                enterPressed = true; // Set enterPressed to true for dialogue or other actions
                fPressed = true;
            } else if (keyCode == KeyEvent.VK_T) {
                // Handle T key for teleport
                tPressed = true;
            } else if (keyCode == KeyEvent.VK_C) {
        gp.gameState = gp.characterState; // Enter character customization
        return; // Prevent this same key event from immediately toggling back
            }
            
        }

        // Pause and music controls
         if (keyCode == KeyEvent.VK_M) {
            // Toggle music on/off
            gp.toggleMusic();
            System.out.println("Music toggled: " + (gp.musicOn ? "ON" : "OFF"));
        }

        // Dialogue control
         if (gp.gameState == gp.dialogState) {
            if (keyCode == KeyEvent.VK_F) {
                gp.gameState = gp.playState; // Exit dialogue state
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                // Exit dialogue state
                gp.gameState = gp.playState;
            }
        }

        // Character customization toggle (exit)
        if (gp.gameState == gp.characterState && keyCode == KeyEvent.VK_C) {
            gp.gameState = gp.playState; // Exit character customization
            return;
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
        } else if (keyCode == KeyEvent.VK_F) {
            fPressed = false; // Reset F key state
        } else if (keyCode == KeyEvent.VK_T) {
            tPressed = false; // Reset T key state
        }
    }

}
