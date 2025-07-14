package src.object;

import java.io.IOException;
import javax.imageio.ImageIO;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Doors extends SuperObject {
    GamePanel gp;
    UtilityTool uTool = new UtilityTool();
    
    public OBJ_Doors(GamePanel gp) {
        this.gp = gp;
        name = "Door";

        // Load door image
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/objects/doors.png"));
            image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
        } catch (IOException e) {
            e.printStackTrace();
        }

        collision = true; // Set collision to true for doors
    }
}
