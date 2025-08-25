package src.main;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import src.entity.Entity;
import src.entity.Player;
import src.tile.TileManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GamePanel extends JPanel implements Runnable {
    // GamePanel implementation goes here
    // This class will handle the game rendering and logic

    // Screen settings
    final int originalTileSize = 16; // 16x16 tiles
    final int scale = 3; // Scale factor

    public final int tileSize = originalTileSize * scale; // 48x48 tiles
    public final int maxScreenCol = 16; // 16 columns
    public final int maxScreenRow = 12; // 12 rows
    public final int screenWidth = tileSize * maxScreenCol; // 768 pixels wide
    public final int screenHeight = tileSize * maxScreenRow; // 576 pixels tall

    // World settings
    public final int maxWorldCol = 50; // 50 columns in the world
    public final int maxWorldRow = 50; // 50 rows in the world
    public final int worldWidth = tileSize * maxWorldCol; // 2400 pixels wide
    public final int worldHeight = tileSize * maxWorldRow; // 2400 pixels

    // Sound settings
    src.main.Sound music = new src.main.Sound(); // Sound manager for handling game sounds
    src.main.Sound se = new src.main.Sound(); // Sound manager for handling game sounds

    // FPS settings
    final int targetFPS = 60; // Target frames per second
    final int maxFPS = 60; // Maximum frames per second

    // System settings
    public src.main.KeyHandler keyHandler = new src.main.KeyHandler(this); // Key handler for input
    Thread gameThread; // Thread for game loop
    public TileManager tileManager = new TileManager(this); // Tile manager for handling tiles
    public src.main.UI ui = new src.main.UI(this); // UI manager for handling the user interface
    public src.main.AssetSetter assetSetter = new src.main.AssetSetter(this); // Asset setter for initializing game
                                                                              // objects
    public src.main.CollisionChecker cChecker = new src.main.CollisionChecker(this); // Collision checker for handling
                                                                                     // collisions

    public EventHandler eventHandler = new EventHandler(this); // Event handler for managing events

    // Entities and objects
    public Player player = new Player(this, keyHandler); // Player entity
    public Entity obj[] = new Entity[10]; // Array to hold game objects
    public Entity npc[] = new Entity[10]; // Array to hold NPCs (Non-Player Characters)
    public Entity monster[] = new Entity[20]; // Array to hold monsters
    ArrayList<Entity> entityList = new ArrayList<>(); // List to hold all entities in the game

    // Game State
    public int gameState;
    public final int titleState = 0; // Game is in title screen state
    public final int playState = 1; // Game is in play state
    public final int pauseState = 2; // Game is paused
    public final int dialogState = 3; // Dialog state for NPC interactions
    public final int characterState = 4; // Character customization state

    // Sound State
    public boolean musicOn = true; // Track if music is on or off

    // Constructor
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // Enable double buffering for smoother rendering
        // Additional initialization code can go here
        this.addKeyListener(keyHandler); // Add key listener for input handling
        this.setFocusable(true); // Make the panel focusable to receive key events
        // Mouse: left-click to attack
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (gameState == playState) {
                        player.startAttack();
                    }
                }
            }
        });

    }

    public void setupGame() {
        assetSetter.setObject(); // Set up game objects
        assetSetter.setNPC(); // Set up NPCs
        assetSetter.setMonster(); // Set up monsters
        // playMusic(0); // Play background music
        gameState = titleState; // Set initial game state to title screen
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start(); // Start the game loop thread
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / targetFPS; // Time per frame in nanoseconds
        double delta = 0; // Time difference accumulator
        long lastTime = System.nanoTime(); // Last time the game was updated
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime(); // Get the current time
            delta += (currentTime - lastTime) / drawInterval; // Calculate the time difference
            lastTime = currentTime; // Update last time

            if (gameState == titleState) {
                // Title state - only repaint for animation
                if (delta >= 1) {
                    repaint(); // Repaint to update title screen animation
                    delta--;
                }
            } else if (gameState == playState) {
                if (delta >= 1) { // If enough time has passed for a frame
                    update(); // Update game logic
                    repaint(); // Render the game
                    delta--; // Decrease delta by 1 to indicate a frame has been processed
                }
            } else if (gameState == dialogState) {
                if (delta >= 1) { // Update during dialog too
                    update(); // Update game logic (for animations)
                    repaint(); // Render the game
                    delta--; // Decrease delta by 1 to indicate a frame has been processed
                }
            } else if (gameState == pauseState) {
                // Only repaint when paused to show pause screen
                repaint();
                delta = 0; // Reset delta to prevent buildup during pause
                try {
                    Thread.sleep(50); // Sleep longer during pause to reduce CPU usage
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue; // Skip the normal sleep at the bottom
            } else if (gameState == characterState) {
                // Character customization: treat like pause (no updates), but repaint for UI
                repaint();
                delta = 0;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            try {
                Thread.sleep(2); // Sleep to prevent high CPU usage
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void update() {
        // Game state handling
        if (gameState == playState) {
            tileManager.update(); // Update tile animations (water, etc.)
            player.update(); // Update player state
            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null) {
                    npc[i].update(); // Update each NPC
                }
            }
            // Update objects (for animations like boots, chests)
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    obj[i].update(); // Update each object
                }
            }
            // Update monsters
            for (int i = 0; i < monster.length; i++) {
                Entity m = monster[i];
                if (m == null)
                    continue;
                if (m.dying) {
                    // Let dying entities advance their fade via draw(); just run a short timer here
                    // if desired
                    m.dyingCounter++;
                    if (m.dyingCounter > 40) { // remove after fade window (~2/3s)
                        monster[i] = null;
                    }
                    continue;
                }
                if (m.alive) {
                    m.update();
                }
            }

        } else if (gameState == dialogState) {
            // During dialog, still update animations but not player movement
            tileManager.update(); // Keep water animations going
            player.update(); // Player handles dialog state internally
            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null) {
                    // Update NPC animations during dialog
                    if (npc[i] instanceof src.entity.NPC_OldMan) {
                        ((src.entity.NPC_OldMan) npc[i]).updateAnimation();
                    }
                }
            }
            // Update objects during dialog too (for animations)
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    obj[i].update(); // Update each object
                }
            }

            // Update monsters during dialog too (for animations)
            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null) {
                    monster[i].update(); // Update each monster
                }
            }

        } else if (gameState == pauseState) {
            // Handle pause state logic if needed
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Render game graphics here
        Graphics2D g2 = (Graphics2D) g;

        // Title screen
        if (gameState == titleState) {
            ui.draw(g2); // Draw only the title screen UI
        }
        // Play state - draw game world
        else if (gameState == playState) {
            // Draw game elements
            tileManager.draw(g2); // Draw the tiles

            entityList.add(player); // Add player to entity list

            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null) {
                    entityList.add(npc[i]); // Add each NPC to entity list
                }
            }

            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    entityList.add(obj[i]); // Add each object to entity list
                }
            }

            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null) {
                    entityList.add(monster[i]); // Add each monster to entity list
                }
            }

            // Sort
            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    return Integer.compare(e1.worldY, e2.worldY);
                }
            });

            // Draw all entities in sorted order
            for (Entity entity : entityList) {
                if (entity != null) {
                    entity.draw(g2, this); // Draw each entity
                }
            }
            // Empty the entity list for the next frame
            entityList.clear(); // Clear all references properly

            // UI
            ui.draw(g2); // Draw the user interface
        }
        // Dialog state - draw game world + dialog UI
        else if (gameState == dialogState) {
            // Draw game elements
            tileManager.draw(g2); // Draw the tiles

            entityList.add(player); // Add player to entity list

            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null) {
                    entityList.add(npc[i]); // Add each NPC to entity list
                }
            }

            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    entityList.add(obj[i]); // Add each object to entity list
                }
            }

            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null) {
                    entityList.add(monster[i]); // Add each monster to entity list
                }
            }

            // Sort
            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    return Integer.compare(e1.worldY, e2.worldY);
                }
            });

            // Draw all entities in sorted order
            for (Entity entity : entityList) {
                if (entity != null) {
                    entity.draw(g2, this); // Draw each entity
                }
            }
            // Empty the entity list for the next frame
            entityList.clear(); // Clear all references properly

            // UI (includes dialog box)
            ui.draw(g2); // Draw the user interface with dialog
        }
        // Pause state - draw game world + pause UI
        else if (gameState == pauseState) {
            // Draw game elements
            tileManager.draw(g2); // Draw the tiles

            entityList.add(player); // Add player to entity list

            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null) {
                    entityList.add(npc[i]); // Add each NPC to entity list
                }
            }

            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null) {
                    entityList.add(obj[i]); // Add each object to entity list
                }
            }

            // Sort
            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    return Integer.compare(e1.worldY, e2.worldY);
                }
            });

            // Draw all entities in sorted order
            for (Entity entity : entityList) {
                if (entity != null) {
                    entity.draw(g2, this); // Draw each entity
                }
            }
            // Empty the entity list for the next frame
            entityList.clear(); // Clear all references properly

            // UI (includes pause screen)
            ui.draw(g2); // Draw the user interface with pause overlay
        }
        // Character customization state - draw game world + character UI
        else if (gameState == characterState) {
            // Draw game elements as background
            tileManager.draw(g2);

            entityList.add(player);
            for (int i = 0; i < npc.length; i++) {
                if (npc[i] != null)
                    entityList.add(npc[i]);
            }
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null)
                    entityList.add(obj[i]);
            }
            for (int i = 0; i < monster.length; i++) {
                if (monster[i] != null)
                    entityList.add(monster[i]);
            }

            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    return Integer.compare(e1.worldY, e2.worldY);
                }
            });
            for (Entity entity : entityList) {
                if (entity != null)
                    entity.draw(g2, this);
            }
            entityList.clear();

            // Draw the character customization UI frame
            ui.draw(g2);
        }

        if (keyHandler.showDebugText) {

            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(Color.WHITE);
            int x = 10;
            int y = 400;
            int lineHeight = 20;

            g2.drawString("WorldX: " + player.worldX, x, y);
            y += lineHeight;
            g2.drawString("WorldY: " + player.worldY, x, y);
            y += lineHeight;
            g2.drawString("Col: " + (player.worldX + player.solidArea.x) / tileSize, x, y);
            y += lineHeight;
            g2.drawString("Row: " + (player.worldY + player.solidArea.y) / tileSize, x, y);
            y += lineHeight;

        }

        g2.dispose(); // Dispose of the graphics context
    }

    public void playMusic(int musicIndex) {
        music.setFile(musicIndex); // Set the music file based on index
        if (musicOn) {
            music.play(); // Play the music only if music is on
            music.loop(); // Loop the music continuously
        }
    }

    public void stopMusic() {
        if (music != null) {
            music.stop(); // Stop the currently playing music
        }
    }

    public void toggleMusic() {
        musicOn = !musicOn; // Toggle music state
        if (musicOn) {
            playMusic(0); // Start music if turned on
        } else {
            stopMusic(); // Stop music if turned off
        }
    }

    public void playSoundEffect(int soundIndex) {
        se.setFile(soundIndex); // Set the sound effect file based on index
        se.play(); // Play the sound effect
    }

}
