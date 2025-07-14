package src.object;

import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Chest extends SuperObject {
    public BufferedImage[] closedFrames; // Animation frames for closed chest
    public BufferedImage[] openFrames;   // Animation frames for opened chest
    public boolean isOpened = false;     // Track if chest is opened
    public boolean isOpening = false;    // Track if chest is currently opening
    public boolean shouldBeRemoved = false; // Track if chest should be removed
    public int openingFrame = 0;         // Current frame in opening animation
    public int openingCounter = 0;       // Counter for opening animation timing
    public int removalDelay = 30;        // Delay before removing the chest (30 frames ≈ 0.5 seconds at 60fps)
    public int removalCounter = 0;       // Counter for removal delay
    
    GamePanel gp;
    UtilityTool uTool = new UtilityTool();

    public OBJ_Chest(GamePanel gp) {
        this.gp = gp;
        name = "Chest";
        hasAnimation = true;
        animationSpeed = 10; // Animation speed for idle chest animation
        
        // Load closed chest animation frames
        closedFrames = new BufferedImage[4];
        openFrames = new BufferedImage[4];
        
        try {
            // Load closed chest frames (idle animation)
            closedFrames[0] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_1.png"));
            closedFrames[1] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_2.png"));
            closedFrames[2] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_3.png"));
            closedFrames[3] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_4.png"));
            
            // Load opened chest frames (opening animation)
            openFrames[0] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_open_1.png"));
            openFrames[1] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_open_2.png"));
            openFrames[2] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_open_3.png"));
            openFrames[3] = ImageIO.read(getClass().getResourceAsStream("/res/objects/chest/chest_open_4.png"));
            
            // Scale all frames
            for (int i = 0; i < closedFrames.length; i++) {
                closedFrames[i] = uTool.scaleImage(closedFrames[i], gp.tileSize, gp.tileSize);
                openFrames[i] = uTool.scaleImage(openFrames[i], gp.tileSize, gp.tileSize);
            }
            
            // Set initial animation frames and default image
            animationFrames = closedFrames;
            image = closedFrames[0];
            
        } catch (IOException e) {
            e.printStackTrace();
            hasAnimation = false; // Disable animation if loading fails
        }
    }
    
    // Method to open the chest
    public void openChest() {
        if (!isOpened && !isOpening) {
            isOpening = true;
            openingFrame = 0;
            openingCounter = 0;
        }
    }
    
    // Override the animation update to handle opening animation
    public void updateAnimation() {
        if (isOpening) {
            // Handle opening animation
            openingCounter++;
            if (openingCounter > 8) { // Slower opening animation
                openingCounter = 0;
                openingFrame++;
                if (openingFrame >= openFrames.length) {
                    // Opening animation finished
                    isOpening = false;
                    isOpened = true;
                    animationFrames = openFrames;
                    animationFrame = openFrames.length - 1; // Stay on last open frame
                    removalCounter = 0; // Start removal countdown
                }
            }
        } else if (isOpened) {
            // Count down before removing the chest
            removalCounter++;
            if (removalCounter >= removalDelay) {
                shouldBeRemoved = true;
            }
        } else if (!isOpened) {
            // Normal idle animation for closed chest
            animationCounter++;
            if (animationCounter > animationSpeed) {
                animationCounter = 0;
                animationFrame++;
                if (animationFrame >= closedFrames.length) {
                    animationFrame = 0;
                }
            }
        }
    }
    
    // Get current frame based on state
    public BufferedImage getCurrentFrame() {
        if (isOpening) {
            return openFrames[openingFrame];
        } else if (isOpened) {
            return openFrames[openFrames.length - 1]; // Last open frame
        } else {
            return closedFrames[animationFrame];
        }
    }
}
