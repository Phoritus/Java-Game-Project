package src.object;

import java.awt.Graphics2D;

import src.entity.Entity;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Axe extends Entity {
    private int drawW;
    private int drawH;

    public OBJ_Axe(GamePanel gp) {
        super(gp);

        type = TYPE_AXE;
        name = "Iron Axe";
        down1 = setup("/res/objects/axe.png");
        price = 10;

        // Reduce axe size to 75% of a tile for both world draw and inventory icon
        int target = (int) Math.round(gp.tileSize * 0.75);
        UtilityTool uTool = new UtilityTool();
        down1 = uTool.scaleImage(down1, target, target);
        drawW = target;
        drawH = target;

        attackValue = 1;
        attackArea.width = 30;
        attackArea.height = 36;
        description = "[" + name + "]\nA sturdy axe.";
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Compute screen position
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Only draw if visible on screen
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            // Center the reduced image inside the tile
            int x = screenX + (gp.tileSize - drawW) / 2;
            int y = screenY + (gp.tileSize - drawH) / 2;
            g2.drawImage(down1, x, y, drawW, drawH, null);
        }
    }
}
