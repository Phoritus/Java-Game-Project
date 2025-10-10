package src.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    // KeyHandler implementation goes here
    // This class will handle key events for the game
    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, fPressed, tPressed, shortKeypress;
    GamePanel gp; // Reference to GamePanel
    public boolean showDebugText = false;
    public boolean godMode = false; // God mode flag
    public boolean bossDebug = false; // Boss debug flag

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
                    gp.resetGame(true); // Start a new game
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
                gp.gameState = (gp.gameState == gp.playState) ? gp.pauseState : gp.playState; // Toggle between play and
                                                                                              // pause
            } else if (keyCode == KeyEvent.VK_F) {
                // Handle F key for healing and other actions
                enterPressed = true; // Set enterPressed to true for dialogue or other actions
                fPressed = true;
            } else if (keyCode == KeyEvent.VK_T) {
                // Handle T key for teleport
                tPressed = true;
            }
            if (keyCode == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.optionState;
                return; // Stop further handling so we don't immediately exit options
            }
            if (keyCode == KeyEvent.VK_E) {
                shortKeypress = true;
            }
            if (keyCode == KeyEvent.VK_C) {
                // Enter character screen; navigation handled below when in characterState
                gp.gameState = gp.characterState;
                return;
            }

        }

        // Character screen navigation and exit
        if (gp.gameState == gp.characterState) {
            playerInventory(keyCode); // Handle inventory navigation
            if (keyCode == KeyEvent.VK_C || keyCode == KeyEvent.VK_ESCAPE) {
                // Exit character screen
                gp.gameState = gp.playState;
                return;
            }
            if (keyCode == KeyEvent.VK_ENTER) {
                // Confirm selection
                // Ensure the cursor doesn't point past the end of the inventory list
                int index = gp.ui.getItemIndexOnslot(gp.ui.playerSlotCol, gp.ui.playerSlotRow);
                if (index >= gp.player.inventory.size()) {
                    return;
                }
                gp.player.selectItem();
            }
            // Consume input in character screen so it doesn't affect gameplay
            return;
        }

        // Dialogue control
        if (gp.gameState == gp.dialogueState || gp.gameState == gp.cutsceneState) {
            if (keyCode == KeyEvent.VK_F) {
                fPressed = true;
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                // Exit dialogue state
                gp.gameState = gp.playState;
            }
        }

        // Option State: limit handling to option-specific keys
        if (gp.gameState == gp.optionState) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.playState; // Exit option state
                return;
            }
            if (keyCode == KeyEvent.VK_ENTER) {
                enterPressed = true;
                return;
            }
            // swallow other keys while options open
            int maxCommandNum = 0;
            switch (gp.ui.subState) {
                case 0:
                    maxCommandNum = 5; // For example, if there are 5 commands (0, 1, 2, 3, 4)
                    break;
                case 1:
                    maxCommandNum = 1; // Apply (0) / Back (1) on fullscreen notice
                    break;

                case 3:
                    maxCommandNum = 1;
                    break;
            }
            if (keyCode == KeyEvent.VK_W) {
                gp.ui.commandNumber--;
                gp.playSoundEffect(9);
                if (gp.ui.commandNumber < 0) {
                    gp.ui.commandNumber = maxCommandNum;
                }
            }
            if (keyCode == KeyEvent.VK_S) {
                gp.ui.commandNumber++;
                gp.playSoundEffect(9);
                if (gp.ui.commandNumber > maxCommandNum) {
                    gp.ui.commandNumber = 0;
                }
            }

            if (keyCode == KeyEvent.VK_A) {
                if (gp.ui.subState == 0) {
                    if (gp.ui.commandNumber == 1 && gp.music.volumeScale > 0) { // Music volume
                        gp.music.volumeScale--;
                        gp.music.checkVolume();
                        gp.playSoundEffect(9);
                        gp.config.saveConfig();
                    }

                    if (gp.ui.commandNumber == 2 && gp.se.volumeScale > 0) { // Sound effects volume
                        gp.se.volumeScale--;
                        gp.playSoundEffect(9);
                        gp.config.saveConfig();
                    }

                }
            }
            if (keyCode == KeyEvent.VK_D) {
                if (gp.ui.subState == 0) {
                    if (gp.ui.commandNumber == 1 && gp.music.volumeScale < 5) { // Music volume
                        gp.music.volumeScale++;
                        gp.music.checkVolume();
                        gp.playSoundEffect(9);
                        gp.config.saveConfig();
                    }
                    if (gp.ui.commandNumber == 2 && gp.se.volumeScale < 5) { // Sound effects volume
                        gp.se.volumeScale++;
                        gp.playSoundEffect(9);
                        gp.config.saveConfig();
                    }

                }
            }

            return;
        }


        // Trade State
        if (gp.gameState == gp.tradeState) {
            tradeState(keyCode);
        }

        // Debugging output
        if (keyCode == KeyEvent.VK_F1) {
            if (!showDebugText) {
                showDebugText = true;
            } else if (showDebugText) {
                showDebugText = false;
            }
        }

        if (gp.gameState == gp.gameOverState) {
            gameOverState(keyCode);
        }

        if (keyCode == KeyEvent.VK_F2) {
            switch (gp.currentMap) {
                case 0:
                    gp.tileManager.loadMap("/res/maps/worldV2.txt", 1);
                    break;
                case 1:
                    gp.tileManager.loadMap("/res/maps/interior01.txt", 0);
                    break;
            }
        }

        // God mode toggle
        if (keyCode == KeyEvent.VK_F3) {
            godMode = !godMode;
            if (godMode) {
                gp.player.invincible = true;
                showDebugText = true;
            } else {
                gp.player.invincible = false;
                showDebugText = false;
            }
        }

        // Boss Debug mode
        if (keyCode == KeyEvent.VK_F4) {
            if (gp.currentMap == 3) { // Only allow toggling in the boss map
                if (!bossDebug) {
                    bossDebug = true;
                } else if (bossDebug) {
                    bossDebug = false;
                }
            }
        }

    }

    public void tradeState(int code) {
        // Navigate
        if (code == KeyEvent.VK_F) {
            // Keep F as an alias for confirm if desired
            enterPressed = true;
            // Also flag fPressed so UI-based handlers can react during trade screens
            fPressed = true;
        }

        if (gp.ui.subState == 0) {
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNumber--;
                if (gp.ui.commandNumber < 0) {
                    gp.ui.commandNumber = 2;
                }
                gp.playSoundEffect(9);
            }

            if (code == KeyEvent.VK_S) {
                gp.ui.commandNumber++;
                if (gp.ui.commandNumber > 2) {
                    gp.ui.commandNumber = 0;
                }
                gp.playSoundEffect(9);
            }

            // Confirm selection with Enter (or F alias)
            if (code == KeyEvent.VK_ENTER || (enterPressed && code == KeyEvent.VK_F)) {
                if (gp.ui.commandNumber == 0) {
                    gp.ui.subState = 1; // Buy
                } else if (gp.ui.commandNumber == 1) {
                    gp.ui.subState = 2; // Sell
                } else if (gp.ui.commandNumber == 2) {
                    // Leave → show farewell dialogue
                    gp.ui.commandNumber = 0;
                    gp.gameState = gp.dialogueState;
                    gp.ui.currentDialogue = "He he, come back anytime.";
                }
                enterPressed = false; // consume
            }
        }

        if (gp.ui.subState == 1) {
            npcInventory(code);
            if (code == KeyEvent.VK_ESCAPE) {
                gp.ui.subState = 0;
            }
        }
        if (gp.ui.subState == 2) {
            playerInventory(code);
            if (code == KeyEvent.VK_ESCAPE) {
                gp.ui.subState = 0;
            }
        }

    }

    public void gameOverState(int code) {
        if (code == KeyEvent.VK_W) {
            gp.ui.commandNumber--;
            if (gp.ui.commandNumber < 0) {
                gp.ui.commandNumber = 1;
            }
            gp.playSoundEffect(9);
        }

        if (code == KeyEvent.VK_S) {
            gp.ui.commandNumber++;
            if (gp.ui.commandNumber > 1) {
                gp.ui.commandNumber = 0;
            }
            gp.playSoundEffect(9);
        }

        if (code == KeyEvent.VK_ENTER) {
            if (gp.ui.commandNumber == 0) {
                gp.gameState = gp.playState;
                gp.retry();
            } else if (gp.ui.commandNumber == 1) {
                gp.gameState = gp.titleState;
                gp.resetGame(true);
            }
        }
    }

    public void playerInventory(int keyCode) {
        // Vertical movement: rows (W/UP = up, S/DOWN = down)
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
            if (gp.ui.playerSlotRow > 0) {
                gp.ui.playerSlotRow--;
                gp.playSoundEffect(9);
            }
        }
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
            if (gp.ui.playerSlotRow < gp.ui.inventoryRows - 1) {
                gp.ui.playerSlotRow++;
                gp.playSoundEffect(9);
            }
        }
        // Horizontal movement: columns (A/LEFT = left, D/RIGHT = right)
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
            if (gp.ui.playerSlotCol > 0) {
                gp.ui.playerSlotCol--;
                gp.playSoundEffect(9);
            }
        }
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
            if (gp.ui.playerSlotCol < gp.ui.inventoryCols - 1) {
                gp.ui.playerSlotCol++;
                gp.playSoundEffect(9);
            }
        }
    }

    public void npcInventory(int keyCode) {
        // Vertical movement: rows (W/UP = up, S/DOWN = down)
        if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
            if (gp.ui.npcSlotRow > 0) {
                gp.ui.npcSlotRow--;
                gp.playSoundEffect(9);
            }
        }
        if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
            if (gp.ui.npcSlotRow < gp.ui.inventoryRows - 1) {
                gp.ui.npcSlotRow++;
                gp.playSoundEffect(9);
            }
        }
        // Horizontal movement: columns (A/LEFT = left, D/RIGHT = right)
        if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
            if (gp.ui.npcSlotCol > 0) {
                gp.ui.npcSlotCol--;
                gp.playSoundEffect(9);
            }
        }
        if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
            if (gp.ui.npcSlotCol < gp.ui.inventoryCols - 1) {
                gp.ui.npcSlotCol++;
                gp.playSoundEffect(9);
            }
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
        if (keyCode == KeyEvent.VK_E) {
            // Reset short press flag when E is released to prevent spamming
            shortKeypress = false;
        }
    }

}
