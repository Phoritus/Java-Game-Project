package src.object;

import java.awt.Graphics2D;

import src.entity.Entity;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Potion extends Entity {
    int value = 5;
    private int drawW;
    private int drawH;
    public OBJ_Potion(GamePanel gp) {
        super(gp);

        this.gp = gp;
        name = "Red Potion";
        down1 = setup("/res/objects/potion.png");
        type = TYPE_CONSUMABLE;
        price = 7;
        description = "[" + name + "]\nHeals " + this.value + " HP.";

        // Reduce size to 75% of tile and remember draw size
        int target = (int) Math.round(gp.tileSize * 0.6 );
        UtilityTool uTool = new UtilityTool();
        if (down1 != null) {
            down1 = uTool.scaleImage(down1, target, target);
        }
        drawW = target;
        drawH = target;
    }

    public boolean use(Entity entity) {
        gp.gameState = gp.dialogState;
        gp.ui.currentDialogue = "You drink the " + name + ".\nYour HP has been restored by " + 
        this.value + ".";

        entity.life += this.value;

        if (gp.player.life > gp.player.maxLife) {
            gp.player.life = gp.player.maxLife;
        }
        gp.playSoundEffect(2);

        return true;
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