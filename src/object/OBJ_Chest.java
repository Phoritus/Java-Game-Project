package src.object;

import java.awt.image.BufferedImage;

import src.entity.Entity;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Chest extends Entity {
    private OBJ_Animation animation; // Animation controller
    public BufferedImage image; // Current image to display
    public String name; // Object name
    public boolean collision = true; // Chest has collision
    public int worldX, worldY; // World position
    
    // Chest specific properties
    public boolean isOpened = false;     // Track if chest is opened
    public boolean isOpening = false;    // Track if chest is currently opening
    public boolean shouldBeRemoved = false; // Track if chest should be removed
    public int removalDelay = 30;        // Delay before removing the chest (30 frames ≈ 0.5 seconds at 60fps)
    public int removalCounter = 0;       // Counter for removal delay
    
    GamePanel gp;
    UtilityTool uTool = new UtilityTool();

    public OBJ_Chest(GamePanel gp) {
        super(gp);
        name = "Chest";
        collision = true; // Chests typically have collision
        
        // Load closed and opened chest animation frames
        BufferedImage[] closedFrames = new BufferedImage[4];
        BufferedImage[] openFrames = new BufferedImage[4];
        
        try {
            // Load closed chest frames (idle animation)
            closedFrames[0] = setup("/res/objects/chest/chest_1.png");
            closedFrames[1] = setup("/res/objects/chest/chest_2.png");
            closedFrames[2] = setup("/res/objects/chest/chest_3.png");
            closedFrames[3] = setup("/res/objects/chest/chest_4.png");

            // Load opened chest frames (opening animation)
            openFrames[0] = setup("/res/objects/chest/chest_open_1.png");
            openFrames[1] = setup("/res/objects/chest/chest_open_2.png");
            openFrames[2] = setup("/res/objects/chest/chest_open_3.png");
            openFrames[3] = setup("/res/objects/chest/chest_open_4.png");
            
            // Scale all frames
            for (int i = 0; i < closedFrames.length; i++) {
                closedFrames[i] = uTool.scaleImage(closedFrames[i], gp.tileSize, gp.tileSize);
                openFrames[i] = uTool.scaleImage(openFrames[i], gp.tileSize, gp.tileSize);
            }
            
            // Create animation controller with closed frames as main and open frames as alternate
            animation = new OBJ_Animation(closedFrames, openFrames, 10);
            
            // Set initial image
            image = closedFrames[0];
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Method to open the chest
    public void openChest() {
        if (!isOpened && !isOpening && animation != null) {
            isOpening = true;
            animation.startOpeningAnimation();
        }
    }
    
    // Update method to handle animation
    public void update() {
        if (animation != null) {
            animation.update();
            image = animation.getCurrentFrame();
            
            // Check if opening animation is complete
            if (isOpening && animation.isAnimationComplete()) {
                isOpening = false;
                isOpened = true;
                removalCounter = 0; // Start removal countdown
            }
        }
        
        // Handle removal countdown
        if (isOpened) {
            removalCounter++;
            if (removalCounter >= removalDelay) {
                shouldBeRemoved = true;
            }
        }
    }
    
    // Method to reset chest state
    public void resetChest() {
        isOpened = false;
        isOpening = false;
        shouldBeRemoved = false;
        removalCounter = 0;
        if (animation != null) {
            animation.switchToMainFrames();
            animation.reset();
        }
    }
    
    // Get current frame
    public BufferedImage getCurrentFrame() {
        return animation != null ? animation.getCurrentFrame() : image;
    }
}
