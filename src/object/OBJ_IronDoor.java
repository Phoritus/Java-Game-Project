package src.object;

import src.entity.Entity;
import java.awt.Rectangle;
import src.main.GamePanel;

public class OBJ_IronDoor extends Entity {

    public OBJ_IronDoor(GamePanel gp) {
        super(gp);
        
        name = "Iron Door";
        type = TYPE_OBSTACLE; // Static obstacle, cannot be removed or unlocked
        collision = true; // Has collision
        
        // Load and scale the image
        down1 = setup("/res/objects/iron_door.png", gp.tileSize, gp.tileSize);
        
        description = "[" + name + "]\nA sturdy iron door.\nCannot be opened.";
        
        // Collision area
        solidArea = new Rectangle(0, 16, 48, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
  
}
