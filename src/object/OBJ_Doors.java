package src.object;

import java.awt.Rectangle;

import src.entity.Entity;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Doors extends Entity {
    UtilityTool uTool = new UtilityTool();
    public boolean unlocked = false;
    
    public OBJ_Doors(GamePanel gp) {
        super(gp);
        name = "Door";
        down1 = setup("/res/objects/doors.png");
        collision = true; // Doors typically have collision
        type = TYPE_OBSTACLE; // Set type to obstacle

        solidArea = new Rectangle(0, 16, 48, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    @Override
    public void update() {
        if (gp == null || gp.player == null) {
            return; // Not yet initialized
        }
        // If player overlaps the door, try to unlock with a key
        // Build rectangles in world space (use default offsets)
        java.awt.Rectangle doorArea = new java.awt.Rectangle(
            worldX + solidAreaDefaultX,
            worldY + solidAreaDefaultY,
            solidArea.width,
            solidArea.height
        );
        java.awt.Rectangle playerArea = new java.awt.Rectangle(
            gp.player.worldX + gp.player.solidAreaDefaultX,
            gp.player.worldY + gp.player.solidAreaDefaultY,
            gp.player.solidArea.width,
            gp.player.solidArea.height
        );

        if (!unlocked && doorArea.intersects(playerArea)) {
            // Check if player has a key in inventory
            int keyIndex = -1;
            for (int i = 0; i < gp.player.inventory.size(); i++) {
                if (gp.player.inventory.get(i) instanceof src.object.OBJ_Key) {
                    keyIndex = i;
                    break;
                }
            }
            if (keyIndex != -1) {
                // Consume key and unlock
                gp.player.inventory.remove(keyIndex);
                gp.playSoundEffect(3); // unlock.wav
                gp.ui.addMessage("Door unlocked!");
                unlocked = true;
                collision = false;
                // Remove this door from the world objects array
                for (int i = 0; i < gp.obj[0].length; i++) {
                    if (gp.obj[gp.currentMap][i] == this) {
                        gp.obj[gp.currentMap][i] = null;
                        break;
                    }
                }
            } else {
                // Optional: show hint if no key
                // gp.ui.addMessage("Need a key");
            }
        }
    }
    
    

}
