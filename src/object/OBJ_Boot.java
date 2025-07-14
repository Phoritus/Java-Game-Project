package src.object;

import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Boot extends SuperObject {
    GamePanel gp; // Reference to GamePanel for accessing game properties
    UtilityTool uTool = new UtilityTool(); // Utility tool for image scaling
    
    public OBJ_Boot(GamePanel gp) {
        this.gp = gp; // Store GamePanel reference
        name = "Boot";
        hasAnimation = true;
        animationSpeed = 20; // Slower animation for boots
        collision = false; // Boots do not have collision
        
        // Load animation frames
        animationFrames = new BufferedImage[2];
        
        try {
            // Load boot animation frames
            animationFrames[0] = ImageIO.read(getClass().getResourceAsStream("/res/objects/boots/boot.png"));
            animationFrames[1] = ImageIO.read(getClass().getResourceAsStream("/res/objects/boots/boot1.png"));
            
            // Scale the images to proper size
            animationFrames[0] = uTool.scaleImage(animationFrames[0], gp.tileSize, gp.tileSize);
            animationFrames[1] = uTool.scaleImage(animationFrames[1], gp.tileSize, gp.tileSize);
            
            // Set the default image to the first frame
            image = animationFrames[0];
            
        } catch (IOException e) {
            e.printStackTrace();
            hasAnimation = false; // Disable animation if loading fails
            
            // Fallback to single image
            try {
                image = ImageIO.read(getClass().getResourceAsStream("/res/objects/boots/boot.png"));
                image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}
