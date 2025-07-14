package src.object;

import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Key extends SuperObject {
    GamePanel gp;
    UtilityTool uTool = new UtilityTool();
    
    public OBJ_Key(GamePanel gp) {
        this.gp = gp;
        name = "Key";
        hasAnimation = true;
        animationSpeed = 12; // Adjust animation speed for keys
        
        // Load animation frames
        animationFrames = new BufferedImage[4];
        
        try {
            // Load all key animation frames
            animationFrames[0] = ImageIO.read(getClass().getResourceAsStream("/res/objects/key/keys_1_1.png"));
            animationFrames[1] = ImageIO.read(getClass().getResourceAsStream("/res/objects/key/keys_1_2.png"));
            animationFrames[2] = ImageIO.read(getClass().getResourceAsStream("/res/objects/key/keys_1_3.png"));
            animationFrames[3] = ImageIO.read(getClass().getResourceAsStream("/res/objects/key/keys_1_4.png"));
            
            // Scale all animation frames
            for (int i = 0; i < animationFrames.length; i++) {
                animationFrames[i] = uTool.scaleImage(animationFrames[i], gp.tileSize, gp.tileSize);
            }
            
            // Set the default image to the first frame
            image = animationFrames[0];
            
        } catch (IOException e) {
            e.printStackTrace();
            hasAnimation = false; // Disable animation if loading fails
        }
    }
}
