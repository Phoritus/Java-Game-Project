package src.main;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import src.entity.Entity;
import src.entity.Player;
import src.environment.EnvironmentManager;
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

    // World settings (expanded to 100x100)
    public final int maxWorldCol = 100; // 100 columns in the world
    public final int maxWorldRow = 100; // 100 rows in the world
    public final int worldWidth = tileSize * maxWorldCol; // 4800 pixels wide
    public final int worldHeight = tileSize * maxWorldRow; // 4800 pixels
    public final int maxMap = 10;

    public int currentMap;

    // Area settings
    public int currentArea;
    public int nextArea;
    public final int indoor = 50;
    public final int outside = 51;
    public final int dungeon = 52;

    // For Full Screen
    int screenWidth2 = screenWidth;
    int screenHeight2 = screenHeight;
    // Offscreen buffers: workScreen is drawn by the game thread, tempScreen is
    // painted by EDT
    private final Object frameLock = new Object();
    BufferedImage tempScreen;
    BufferedImage workScreen;
    Graphics2D g2; // Graphics bound to workScreen for building the next frame
    public boolean fullscreenOn = false;

    // Render scale: keep at 1.0 so internal resolution remains the same.
    private double renderScale = 1.0;

    // Sound settings
    src.main.Sound music = new src.main.Sound(); // Sound manager for handling game sounds
    src.main.Sound se = new src.main.Sound(); // Sound manager for handling game sounds

    // FPS settings
    final int targetFPS = 60; // Target frames per second
    final int maxFPS = 60; // Maximum frames per second

    // System settings
    public src.main.KeyHandler keyHandler = new src.main.KeyHandler(this); // Key handler for input
    EnvironmentManager envManager = new EnvironmentManager(this); // Environment manager for weather, time, etc.
    public CutsceneManager cutsceneManager = new CutsceneManager(this);
    Thread gameThread; // Thread for game loop
    Config config = new Config(this);
    public TileManager tileManager = new TileManager(this); // Tile manager for handling tiles
    public src.main.UI ui = new src.main.UI(this); // UI manager for handling the user interface
    public src.main.AssetSetter assetSetter = new src.main.AssetSetter(this); // Asset setter for initializing game
                                                                              // objects
    public src.main.CollisionChecker cChecker = new src.main.CollisionChecker(this); // Collision checker for handling
                                                                                     // collisions

    public EventHandler eventHandler = new EventHandler(this); // Event handler for managing events

    // Entities and objects
    public Player player = new Player(this, keyHandler); // Player entity
    public Entity obj[][] = new Entity[maxMap][50]; // Array to hold game objects (increased capacity)
    public Entity npc[][] = new Entity[maxMap][10]; // Array to hold NPCs (Non-Player Characters)
    public InteractiveTile iTile[][] = new InteractiveTile[maxMap][30]; // Array to hold interactive tiles
    public Entity monster[][] = new Entity[maxMap][20]; // Array to hold monsters
    ArrayList<Entity> entityList = new ArrayList<>(); // List to hold all entities in the game
    public ArrayList<Entity> particleList = new ArrayList<>(); // List to hold all particles in the game
    public ArrayList<Entity> projectileList = new ArrayList<>(); // List to hold all projectiles in the game

    // Game State
    public int gameState;
    public final int titleState = 0; // Game is in title screen state
    public final int playState = 1; // Game is in play state
    public final int pauseState = 2; // Game is paused
    public final int dialogueState = 3; // Dialogue state for NPC interactions
    public final int characterState = 4; // Character customization state
    public final int gameOverState = 6;
    public final int optionState = 7;
    public final int transitionState = 8;
    public final int tradeState = 9;
    public final int sleepState = 10;
    public final int cutsceneState = 11;

    // Other
    public boolean bossBattleOn = false;

    // Sound State
    public boolean musicOn = true; // Track if music is on or off
    // One-shot flag to indicate a full game reset is occurring
    public boolean resettingGame = false;

    // Constructor
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(false);
        
        try {
            javax.swing.RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
        } catch (Exception ignore) {
        }
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

    public void resetGame(boolean restart) {
        resettingGame = true;

        // Stop any audio and clear temp entities
        stopMusic();
        removeTempEntities();

        // Reset boss/cutscene/ending state so nothing continues running
        bossBattleOn = false;
        if (cutsceneManager != null) {
            cutsceneManager.sceneNum = 0; // NA
            cutsceneManager.scenePhase = 0;
            cutsceneManager.counter = 0;
            try {
                // Reset visual helpers used by ending scene
                java.lang.reflect.Field alphaField = cutsceneManager.getClass().getDeclaredField("alpha");
                alphaField.setAccessible(true);
                alphaField.setFloat(cutsceneManager, 0f);
                java.lang.reflect.Field yField = cutsceneManager.getClass().getDeclaredField("y");
                yField.setAccessible(true);
                yField.setInt(cutsceneManager, 0);
            } catch (Exception ignore) {
            }
        }

        // Clear any active dialogue/cutscene speaker and UI transient state
        if (ui != null) {
            ui.npc = null;
            ui.currentDialogue = "";
            ui.messageOn = false;
            try {
                java.util.List<?> msgs = (java.util.List<?>) ui.getClass().getDeclaredField("messages").get(ui);
                java.util.List<?> ctrs = (java.util.List<?>) ui.getClass().getDeclaredField("messageCounters").get(ui);
                msgs.clear();
                ctrs.clear();
            } catch (Exception ignore) {
            }
            ui.charIndex = 0;
            ui.combinedText = "";
            ui.typewriterCounter = 0;
            ui.titleAnimationCounter = 0;
            ui.titleCurrentFrame = 0;
            // Reset menu focus defaults
            ui.commandNumber = 0;
        }

        // Clear live effects/entities that could leak across states
        projectileList.clear();
        particleList.clear();
        // Remove lingering PlayerDummy if any
        for (int map = 0; map < maxMap; map++) {
            for (int i = 0; i < npc[map].length; i++) {
                if (npc[map][i] instanceof src.entity.PlayerDummy) {
                    npc[map][i] = null;
                }
            }
        }

        // Reset input latches to avoid immediate re-trigger
        keyHandler.enterPressed = false;
        keyHandler.fPressed = false;
        keyHandler.tPressed = false;

        // Reset player and world content
        player.setDefaultValues();
        player.setItems();
        assetSetter.setObject();
        assetSetter.setNPC();
        assetSetter.setMonster();
        assetSetter.setInteractiveTile();
        envManager.setup();
        // Ensure starting area is outside (daytime brightness behavior)
        currentArea = outside;
        nextArea = outside;
        if (restart) {
            player.setDefaultPositions();
            player.restoreLifeAndMana();
        } else {
            player.worldX = tileSize * 26;
            player.worldY = tileSize * 23;
        }

        // Reset lighting/brightness to default daylight on reset
        try {
            if (envManager != null && envManager.lighting != null) {
                envManager.lighting.filterAlpha = 0f;
                envManager.lighting.dayState = envManager.lighting.day;
                envManager.lighting.setLightSource();
            }
        } catch (Exception ignore) {
        }

        // Clear the resetting flag so normal drawing resumes
        resettingGame = false;

        // Reset event system so story/cutscene triggers can happen again cleanly
        if (eventHandler != null) {
            eventHandler.canTouchEvent = true;
            eventHandler.previousEventX = player.worldX;
            eventHandler.previousEventY = player.worldY;
            // Clear eventDone flags across all maps/tiles
            try {
                src.main.EventRect[][][] ers = (src.main.EventRect[][][]) eventHandler.getClass()
                        .getDeclaredField("eventRect").get(eventHandler);
                for (int m = 0; m < ers.length; m++) {
                    for (int c = 0; c < ers[m].length; c++) {
                        for (int r = 0; r < ers[m][c].length; r++) {
                            if (ers[m][c][r] != null) {
                                ers[m][c][r].eventDone = false;
                            }
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }

    }

    public void setupGame() {
        assetSetter.setObject(); // Set up game objects
        assetSetter.setNPC(); // Set up NPCs
        assetSetter.setMonster(); // Set up monsters
        assetSetter.setInteractiveTile();
        envManager.setup(); // Set up environment manager
        // playMusic(0); // Play background music
        gameState = titleState; // Set initial game state to title screen
        currentArea = outside;

        // Create both back buffers up front so swaps never hit null
        tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        workScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) workScreen.getGraphics();
        // Reset any transforms on fresh graphics
        g2.setTransform(new AffineTransform());
        g2.scale(renderScale, renderScale);

        // Apply display mode based on config loaded in Main
        if (fullscreenOn) {
            setFullScreen();
        } else {
            setWindowed(1280, 720);
        }
    }

    // Ensure buffers exist if something recreated Graphics or after external
    // changes
    private void ensureBuffers() {
        if (tempScreen == null) {
            tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        }
        if (workScreen == null) {
            workScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        }
        if (g2 == null) {
            g2 = (Graphics2D) workScreen.getGraphics();
            g2.setTransform(new AffineTransform());
            g2.scale(renderScale, renderScale);
        }
    }

    public void retry() {
        player.restoreLifeAndMana();
        // 1) Restore player first
        player.restoreLifeAndMana();

        // 2) Switch back to play state immediately
        gameState = playState;

        // 4) Reset boss/cutscene state so minotour cutscene can trigger again
        bossBattleOn = false;
        if (cutsceneManager != null) {
            cutsceneManager.sceneNum = 0; // NA
            cutsceneManager.scenePhase = 0;
            cutsceneManager.counter = 0;
        }
        if (ui != null)
            ui.npc = null;

        // Remove lingering PlayerDummy if any
        for (int map = 0; map < maxMap; map++) {
            for (int i = 0; i < npc[map].length; i++) {
                if (npc[map][i] instanceof src.entity.PlayerDummy) {
                    npc[map][i] = null;
                }
            }
        }
        // 6) Reposition and repopulate actors
        player.setDefaultPositions();

        // 7) Give brief i-frames on respawn
        player.invincible = true;
        player.invincibleCounter = 0;
    }

    public void setFullScreen() {
        // Ensure undecorated when entering fullscreen
        try {
            Main.window.setVisible(false);
            Main.window.dispose();
            Main.window.setUndecorated(true);
        } catch (Exception ignore) {
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(Main.window);

        // Get full screen width and height via display mode when possible
        java.awt.DisplayMode dm = gd.getDisplayMode();
        if (dm != null) {
            screenWidth2 = dm.getWidth();
            screenHeight2 = dm.getHeight();
        } else {
            screenWidth2 = Main.window.getWidth();
            screenHeight2 = Main.window.getHeight();
        }
    }

    public void setWindowed(int width, int height) {
        // Exit exclusive fullscreen if set
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        try {
            if (gd.getFullScreenWindow() != null) {
                gd.setFullScreenWindow(null);
            }
        } catch (Exception ignore) {
        }

        // Switch to decorated window and size it
        try {
            Main.window.setVisible(false);
            Main.window.dispose();
            Main.window.setUndecorated(false);
            Main.window.setResizable(true);
        } catch (Exception ignore) {
        }

        this.setPreferredSize(new Dimension(width, height));
        this.setSize(width, height);
        Main.window.add(this);
        Main.window.pack();
        Main.window.setSize(width, height);
        Main.window.setLocationRelativeTo(null);
        Main.window.setVisible(true);
        this.revalidate();
        this.repaint();

        // Update current draw target size used by drawToScreen/paint scaling
        screenWidth2 = width;
        screenHeight2 = height;
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

            if (delta >= 1) {
                update(); // Update game state
                drawToTempScreen(); // Draw to the offscreen buffer
                // Let Swing paint the latest tempScreen buffer
                repaint();
                delta--;
            }

        }
    }

    public void update() {
        // Game state handling
        if (gameState == playState) {
            tileManager.update(); // Update tile animations (water, etc.)
            player.update(); // Update player state

            for (int i = 0; i < npc[0].length; i++) {
                if (npc[currentMap][i] != null) {
                    npc[currentMap][i].update(); // Update each NPC
                }
            }
            // Update objects (for animations like boots, chests)
            for (int i = 0; i < obj[0].length; i++) {
                if (obj[currentMap][i] != null) {
                    obj[currentMap][i].update(); // Update each object
                }
            }
            // Update monsters
            for (int i = 0; i < monster[0].length; i++) {
                Entity m = monster[currentMap][i];
                if (m == null)
                    continue;
                if (m.dying) {
                    // Let dying entities advance their fade via draw(); just run a short timer here
                    // if desired
                    m.dyingCounter++;
                    if (m.dyingCounter > 40) { // remove after fade window (~2/3s)
                        monster[currentMap][i].checkDrop();
                        monster[currentMap][i] = null;
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

            for (int i = 0; i < iTile[0].length; i++) {
                if (iTile[currentMap][i] != null) {
                    iTile[currentMap][i].update(); // Update each interactive tile
                }
            }

        } else if (gameState == dialogueState) {
            // During dialog, still update animations but not player movement
            tileManager.update(); // Keep water animations going
            player.update(); // Player handles dialog state internally
            for (int i = 0; i < npc[0].length; i++) {
                if (npc[currentMap][i] != null) {
                    // Update NPC animations during dialog
                    if (npc[currentMap][i] instanceof src.entity.NPC_OldMan) {
                        ((src.entity.NPC_OldMan) npc[currentMap][i]).updateAnimation();
                    }
                }
            }
            // Update objects during dialog too (for animations)
            for (int i = 0; i < obj[0].length; i++) {
                if (obj[currentMap][i] != null) {
                    obj[currentMap][i].update(); // Update each object
                }
            }

            // Update monsters during dialog too (for animations)
            for (int i = 0; i < monster[0].length; i++) {
                if (monster[currentMap][i] != null) {
                    monster[currentMap][i].update(); // Update each monster
                }
            }

        } else if (gameState == pauseState) {
            // Handle pause state logic if needed
        }
        envManager.update(); // Update environment (lighting, weather, etc.)
    }

    public void drawToTempScreen() {
        // Defensive: make sure buffers/graphics exist
        ensureBuffers();
        // Ensure transform is applied each frame (fresh Graphics on swap)
        g2.setTransform(new AffineTransform());
        g2.scale(renderScale, renderScale);
        // Clear the work buffer before drawing the new frame (logical coords)
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        // Title screen only: UI draws title with its own background
        if (gameState == titleState) {
            ui.draw(g2);
        } else {
            // Default path for all non-title states: draw world once, then overlay UI.
            tileManager.draw(g2);
            for (int i = 0; i < iTile[0].length; i++) {
                if (iTile[currentMap][i] != null) {
                    iTile[currentMap][i].draw(g2);
                }
            }

            entityList.add(player);
            for (int i = 0; i < npc[0].length; i++) {
                if (npc[currentMap][i] != null)
                    entityList.add(npc[currentMap][i]);
            }
            for (int i = 0; i < obj[0].length; i++) {
                if (obj[currentMap][i] != null)
                    entityList.add(obj[currentMap][i]);
            }
            for (int i = 0; i < monster[0].length; i++) {
                if (monster[currentMap][i] != null)
                    entityList.add(monster[currentMap][i]);
            }
            for (int i = 0; i < projectileList.size(); i++) {
                if (projectileList.get(i) != null && projectileList.get(i).alive) {
                    entityList.add(projectileList.get(i));
                }
            }
            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null && particleList.get(i).alive) {
                    entityList.add(particleList.get(i));
                }
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

            // Environment (weather, time)
            envManager.draw(g2);

            // Cutscenes
            cutsceneManager.draw(g2);

            // Overlay per-state UI (pause, dialog, trade, options, transition, etc.)
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
            g2.drawString("God Mode:" + keyHandler.godMode, x, y);
            y += lineHeight;
            g2.drawString("Boss Debug Mode:" + keyHandler.bossDebug, x, y);
        }

        // Swap buffers atomically so paintComponent always reads a complete frame
        synchronized (frameLock) {
            // Defensive: if either buffer got nulled, recreate
            if (tempScreen == null || workScreen == null) {
                if (tempScreen == null)
                    tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
                if (workScreen == null)
                    workScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
            }
            BufferedImage swap = tempScreen;
            tempScreen = workScreen;
            workScreen = swap;
            g2 = (Graphics2D) workScreen.getGraphics();
            // Reapply transform for the new work buffer
            g2.setTransform(new AffineTransform());
            g2.scale(renderScale, renderScale);
        }

    }

    // No direct draw-to-screen; paintComponent handles presenting tempScreen.

    @Override
    protected void paintComponent(Graphics g) {
        // Avoid Swing's default background clear to reduce flicker in fullscreen; we
        // draw a full-frame buffer below
        BufferedImage toDraw;
        synchronized (frameLock) {
            toDraw = tempScreen;
        }
        if (toDraw == null)
            return;
        // Letterbox-scale the logical buffer to the current component size
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0)
            return;

        // Compute cover scaling (fill window, crop excess) to avoid black bars
        double scaleX = (double) w / (double) screenWidth;
        double scaleY = (double) h / (double) screenHeight;
        double scale = Math.max(scaleX, scaleY); // cover fit
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

        // On Windows, ensure display sync to reduce tearing/flicker
        try {
            java.awt.Toolkit.getDefaultToolkit().sync();
        } catch (Exception ignore) {
        }
    }

    public void playMusic(int musicIndex) {
        music.setFile(musicIndex); // Set the music file based on index
        if (musicOn) {
            music.play(); // Play the music only if music is on
            music.loop(); // Loop the music continuously
        }
    }

    // Play background music once (no loop), useful for endings/short themes
    public void playMusicOnce(int musicIndex) {
        music.setFile(musicIndex);
        if (musicOn) {
            music.play();
            // Do NOT loop
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

    public void changeArea() {
        if (nextArea != currentArea) {

            stopMusic();
            if (nextArea == outside) {
                playMusic(0);
            } else if (nextArea == indoor) {
                playMusic(18);
            } else if (nextArea == dungeon) {
                playMusic(17);
            }
        }
        currentArea = nextArea;
        assetSetter.setMonster();

    }

    public void removeTempEntities() {

        for (int mapNum = 0; mapNum < maxMap; mapNum++) {
            for (int i = 0; i < obj[0].length; i++) {
                if (obj[mapNum][i] != null && obj[mapNum][i].temp) {
                    obj[mapNum][i] = null;
                }
            }
        }
    }

}