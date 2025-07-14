package src.main;

import javax.swing.JPanel;

import src.entity.Player;
import src.object.SuperObject;
import src.tile.TileManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
    src.main.KeyHandler keyHandler = new src.main.KeyHandler(this); // Key handler for input
    Thread gameThread; // Thread for game loop
    public TileManager tileManager = new TileManager(this); // Tile manager for handling tiles
    public src.main.UI ui = new src.main.UI(this); // UI manager for handling the user interface
    public src.main.AssetSetter assetSetter = new src.main.AssetSetter(this); // Asset setter for initializing game objects
    public src.main.CollisionChecker cChecker = new src.main.CollisionChecker(this); // Collision checker for handling collisions
    
    // Entities and objects
    public Player player = new Player(this, keyHandler); // Player entity
    public SuperObject obj[] = new SuperObject[10]; // Array to hold game objects

    // Game State
    public int gameState;
    public final int playState = 1; // Game is in play state
    public final int pauseState = 2; // Game is paused
    
    // Sound State
    public boolean musicOn = true; // Track if music is on or off


    // Constructor
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);  // Enable double buffering for smoother rendering
        // Additional initialization code can go here
        this.addKeyListener(keyHandler); // Add key listener for input handling
        this.setFocusable(true); // Make the panel focusable to receive key events

    }

    public void setupGame() {
        assetSetter.setObject(); // Set up game objects
        playMusic(0); // Play background music
        gameState = playState; // Set initial game state to play

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
        long lastFPSCheck = System.nanoTime(); // Last time FPS was checked

        while (gameThread != null) {
            currentTime = System.nanoTime(); // Get the current time
            delta += (currentTime - lastTime) / drawInterval; // Calculate the time difference
            lastTime = currentTime; // Update last time
            
            if (gameState == playState) {
                if (currentTime - lastFPSCheck >= 1000000000) { // Check FPS every second
                    System.out.println("FPS: " + targetFPS); // Print current FPS
                    lastFPSCheck = currentTime; // Update last FPS check time
                }
                
                if (delta >= 1) { // If enough time has passed for a frame
                    update(); // Update game logic
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
            // Update game time only when playing
            ui.gameTime += 1.0/60.0; // Assuming 60 FPS
            
            // Update game logic for play state
            player.update(); // Update player position and state
            tileManager.update(); // Update tile animations
            
            // Check for chests that should be removed after opening
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] != null && obj[i] instanceof src.object.OBJ_Chest) {
                    src.object.OBJ_Chest chest = (src.object.OBJ_Chest) obj[i];
                    if (chest.shouldBeRemoved) {
                        obj[i] = null; // Remove the chest from the game
                        System.out.println("Chest removed after opening!");
                    }
                }
            }
        } else if (gameState == pauseState) {
            // Don't update anything during pause - game is truly paused
            // System.out.println("Game is paused - state: " + gameState);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Render game graphics here
        Graphics2D g2 = (Graphics2D) g;

        //Debugging grid
        long drawStart = System.nanoTime(); // Start time for rendering

        // Always draw game elements (even when paused)
        tileManager.draw(g2); // Draw the tiles

        // Objects
        for (SuperObject obj : this.obj) {
            if (obj != null) {
                obj.draw(g2, this); // Draw each object
            }
        }

        player.draw(g2); // Draw the player on the screen
        
        // Show debug info only during play
        if (gameState == playState) {
            long drawEnd = System.nanoTime(); // End time for rendering
            double drawTime = (drawEnd - drawStart) / 1000000.0;
            g2.setColor(Color.WHITE); // Set text color to white for better visibility
            g2.drawString("Draw Time: " + String.format("%.2f", drawTime) + " ms", 10, screenHeight - 20); // Display draw time at bottom
            System.out.println("Draw Time: " + drawTime + " ms"); // Print draw time to console
        }

        // UI (always draw - handles pause screen and game time)
        ui.draw(g2); // Draw the user interface

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
        music.stop(); // Stop the currently playing music
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
