package src.object;

import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;

import src.entity.Entity;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Doors extends Entity {
    GamePanel gp;
    UtilityTool uTool = new UtilityTool();
    
    public OBJ_Doors(GamePanel gp) {
        super(gp);
        name = "Door";
        down1 = setup("/res/objects/doors.png");
        collision = true; // Doors typically have collision

        solidArea = new Rectangle(0, 16, 48, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }
    
    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Call parent draw method first to draw the door image
        super.draw(g2, gp);
        
        // Draw hit box for debugging
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            
            // Calculate screen position
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            
            // Draw collision area as red rectangle
            g2.setColor(Color.RED);
            g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, 
                       solidArea.width, solidArea.height);
            
            // Draw a semi-transparent fill
            g2.setColor(new Color(255, 0, 0, 50)); // Red with 50 alpha
            g2.fillRect(screenX + solidArea.x, screenY + solidArea.y, 
                       solidArea.width, solidArea.height);
        }
    }
}
