package src.main;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import src.entity.Entity;
import src.entity.Player;
import src.tile.TileManager;
import src.tiles_interactive.InteractiveTile;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
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
    public final int maxScreenCol = 20;
    public final int maxScreenRow = 12; // 12 rows
    public final int screenWidth = tileSize * maxScreenCol; // 768 pixels wide
    public final int screenHeight = tileSize * maxScreenRow; // 576 pixels tall

    // World settings
    public final int maxWorldCol = 50; // 50 columns in the world
    public final int maxWorldRow = 50; // 50 rows in the world
    public final int worldWidth = tileSize * maxWorldCol; // 2400 pixels wide
    public final int worldHeight = tileSize * maxWorldRow; // 2400 pixels

    //For Full Screen
    int screenWidth2 = screenWidth;
    int screenHeight2 = screenHeight;
    // Offscreen buffers: workScreen is drawn by the game thread, tempScreen is painted by EDT
    private final Object frameLock = new Object();
    BufferedImage tempScreen;
    BufferedImage workScreen;
    Graphics2D g2; // Graphics bound to workScreen for building the next frame

    // Render scale: keep at 1.0 so internal resolution remains the same.
    private double renderScale = 1.0;

    // Fullscreen/windowed control
    private boolean fullscreenEnabled = false; // default to windowed
    private int windowedWidth = 1280;
    private int windowedHeight = 720;

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
    public InteractiveTile iTile[] = new InteractiveTile[30]; // Array to hold interactive tiles
    public Entity monster[] = new Entity[20]; // Array to hold monsters
    ArrayList<Entity> entityList = new ArrayList<>(); // List to hold all entities in the game
    public ArrayList<Entity> particleList = new ArrayList<>(); // List to hold all particles in the game
    public ArrayList<Entity> projectileList = new ArrayList<>(); // List to hold all projectiles in the game

    // Game State
    public int gameState;
    public final int titleState = 0; // Game is in title screen state
    public final int playState = 1; // Game is in play state
    public final int pauseState = 2; // Game is paused
    public final int dialogState = 3; // Dialog state for NPC interactions
    public final int characterState = 4; // Character customization state
    public final int gameOverState = 6;
    public final int optionState = 7;

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
        assetSetter.setInteractiveTile();
        // playMusic(0); // Play background music
        gameState = titleState; // Set initial game state to title screen
    // Choose display mode (windowed by default)
    if (fullscreenEnabled) {
        setFullScreen();
    } else {
        setWindowed(windowedWidth, windowedHeight);
    }
    // Create the back buffers at reduced internal resolution
    int renderW = (int) Math.round(screenWidth * renderScale);
    int renderH = (int) Math.round(screenHeight * renderScale);
    tempScreen = new BufferedImage(renderW, renderH, BufferedImage.TYPE_INT_ARGB);
    workScreen = new BufferedImage(renderW, renderH, BufferedImage.TYPE_INT_ARGB);
    g2 = (Graphics2D) workScreen.getGraphics();
    // Set transform so game draws using logical coordinates into smaller buffer
    g2.setTransform(new AffineTransform());
    g2.scale(renderScale, renderScale);
    }

    public void setFullScreen(){

        // Get local screen device
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        try {
            // Ensure proper decoration state for fullscreen
            Main.window.setVisible(false);
            Main.window.dispose();
            Main.window.setUndecorated(true);
            Main.window.setResizable(false);
        } catch (Exception ignore) {}
        try {
            gd.setFullScreenWindow(Main.window);
        } catch (Exception e) {
            // Fallback: borderless maximized window covers screen
            Main.window.setExtendedState(Main.window.getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
        }

    // Get full screen width and height using the display mode (more reliable here)
        java.awt.DisplayMode dm = gd.getDisplayMode();
        if (dm != null) {
            screenWidth2 = dm.getWidth();
            screenHeight2 = dm.getHeight();
        } else {
            // Fallback to the window size if display mode unavailable
            screenWidth2 = Main.window.getWidth();
            screenHeight2 = Main.window.getHeight();
        }

    // Ensure this panel/layout knows about the new size
    this.setPreferredSize(new Dimension(screenWidth2, screenHeight2));
    this.setSize(screenWidth2, screenHeight2);
    Main.window.setSize(screenWidth2, screenHeight2);
    Main.window.revalidate();
    Main.window.validate();
    this.revalidate();
    this.repaint();
    }

    public void setWindowed(int width, int height) {
        // Exit exclusive fullscreen if set
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        try {
            if (gd.getFullScreenWindow() != null) {
                gd.setFullScreenWindow(null);
            }
        } catch (Exception ignore) {}

        // Switch to decorated window and size it
        try {
            Main.window.setVisible(false);
            Main.window.dispose();
            Main.window.setUndecorated(false);
            Main.window.setResizable(true);
        } catch (Exception ignore) {}

        this.setPreferredSize(new Dimension(width, height));
        this.setSize(width, height);
        Main.window.add(this);
        Main.window.pack();
        Main.window.setSize(width, height);
        Main.window.setLocationRelativeTo(null);
        Main.window.setVisible(true);
        this.revalidate();
        this.repaint();
    }

    public void toggleFullscreen() {
        fullscreenEnabled = !fullscreenEnabled;
        if (fullscreenEnabled) {
            setFullScreen();
        } else {
            setWindowed(windowedWidth, windowedHeight);
        }
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
                    drawToTempScreen();
                    drawToScreen();
                    delta--;
                }
            } else if (gameState == playState) {
                if (delta >= 1) { // If enough time has passed for a frame
                    update(); // Update game logic
                    drawToTempScreen(); // Render the game
                    drawToScreen();
                    delta--; // Decrease delta by 1 to indicate a frame has been processed
                }
            } else if (gameState == dialogState) {
                if (delta >= 1) { // Update during dialog too
                    update(); // Update game logic (for animations)
                    drawToTempScreen(); // Render the game
                    drawToScreen();
                    delta--; // Decrease delta by 1 to indicate a frame has been processed
                }
            } else if (gameState == pauseState) {
                // Only repaint when paused to show pause screen
                drawToTempScreen();
                drawToScreen();
                delta = 0; // Reset delta to prevent buildup during pause
                try {
                    Thread.sleep(50); // Sleep longer during pause to reduce CPU usage
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue; // Skip the normal sleep at the bottom
            } else if (gameState == characterState) {
                // Character customization: treat like pause (no updates), but repaint for UI
                drawToTempScreen();
                drawToScreen();
                delta = 0;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else if (gameState == optionState) {
                // Options menu: draw without updating gameplay
                drawToTempScreen();
                drawToScreen();
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
                        monster[i].checkDrop();
                        monster[i] = null;
                    }
                    continue;
                }
                if (m.alive) {
                    m.update();
                }
            }

            // Update and remove projectiles (iterate backwards to avoid skipping on remove)
            for (int i = projectileList.size() - 1; i >= 0; i--) {
                Entity p = projectileList.get(i);
                if (p == null) {
                    projectileList.remove(i);
                    continue;
                }
                if (p.alive) {
                    p.update();
                }
                if (!p.alive) {
                    projectileList.remove(i);
                }
            }

            for (int i = particleList.size() - 1; i >= 0; i--) {
                Entity p = particleList.get(i);
                if (p == null) {
                    particleList.remove(i);
                    continue;
                }
                if (p.alive) {
                    p.update();
                }
                if (!p.alive) {
                    particleList.remove(i);
                }
            }

            for (int i = 0; i < iTile.length; i++) {
                if (iTile[i] != null) {
                    iTile[i].update(); // Update each interactive tile
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

    public void drawToTempScreen() {
    // Ensure transform is applied each frame (fresh Graphics on swap)
    g2.setTransform(new AffineTransform());
    g2.scale(renderScale, renderScale);
    // Clear the work buffer before drawing the new frame (logical coords)
    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, screenWidth, screenHeight);
        // Title screen
        if (gameState == titleState) {
            ui.draw(g2); // Draw only the title screen UI
        }
        // Play state - draw game world
        else if (gameState == playState) {
            // Draw game elements
            tileManager.draw(g2); // Draw the tiles
            for (int i = 0; i < iTile.length; i++) {
                if (iTile[i] != null) {
                    iTile[i].draw(g2); // Draw each interactive tile
                }
            }

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

            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null && projectileList.get(i).alive) {
                    entityList.add(projectileList.get(i)); // Add alive projectiles only
                }
            }
            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null && particleList.get(i).alive) {
                    entityList.add(particleList.get(i)); // Add alive particles only
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
        // Option state - draw game world + options UI
        else if (gameState == optionState) {
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

            // Draw the options UI frame
            ui.draw(g2);
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

        // Swap buffers atomically so paintComponent always reads a complete frame
        synchronized (frameLock) {
            BufferedImage swap = tempScreen;
            tempScreen = workScreen;
            workScreen = swap;
            g2 = (Graphics2D) workScreen.getGraphics();
            // Reapply transform for the new work buffer
            g2.setTransform(new AffineTransform());
            g2.scale(renderScale, renderScale);
        }

    }
    
    public void drawToScreen(){
        // Let Swing handle painting; this avoids flicker and centers consistently
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage toDraw;
        synchronized (frameLock) {
            toDraw = tempScreen;
        }
        if (toDraw == null) return;
        // Letterbox-scale the logical buffer to the current component size
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Clear background (letterbox bars)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);

    // tempScreen is smaller; compute scale based on logical size
    double scaleX = (double) w / (double) screenWidth;
    double scaleY = (double) h / (double) screenHeight;
    double scale = Math.min(scaleX, scaleY);
    int drawW = (int) Math.round(screenWidth * scale);
    int drawH = (int) Math.round(screenHeight * scale);
    int dx = (w - drawW) / 2;
    int dy = (h - drawH) / 2;

    // Use nearest-neighbor scaling to keep pixel art crisp
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
        java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
        java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
    g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
        java.awt.RenderingHints.VALUE_RENDER_SPEED);

    g2d.drawImage(toDraw, dx, dy, drawW, drawH, null);
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
