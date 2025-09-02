package src.tiles_interactive;

import java.awt.Color;

import src.entity.Entity;
import src.main.GamePanel;

public class IT_DryTree extends InteractiveTile{
    GamePanel gp;

    public IT_DryTree(GamePanel gp, int col, int row) {
        super(gp, col, row);
        this.gp = gp;

        this.worldX = gp.tileSize * col;
        this.worldY = gp.tileSize * row;

        down1 = setup("/res/tiles_interactive/drytree.png");
        destructible = true;
        life = 3;
    // Make the dry tree block almost the entire tile
    // Small padding keeps movement feeling natural against corners
    this.collision = true;
    this.solidArea.x = 2;
    this.solidArea.y = 2;
    this.solidArea.width = gp.tileSize - 4;
    this.solidArea.height = gp.tileSize - 4;
    this.solidAreaDefaultX = this.solidArea.x;
    this.solidAreaDefaultY = this.solidArea.y;
    }

    public void playSE() {
        gp.playSoundEffect(11);
    }

    public InteractiveTile getDestroyedForm() {
        InteractiveTile tile = new IT_Trunk(gp, worldX/gp.tileSize, worldY/gp.tileSize);
        return tile;
    }
    public boolean isCorrectItem(Entity entity) {
        boolean isCorrectItem = false;
        if (entity.currentWeapon.type == TYPE_AXE) {
            isCorrectItem = true;
        }
        return isCorrectItem;
    }

    public Color getParticleColor() {
        return new Color(65, 50, 30);
    }

    public int getParticleSize() {
        return 6;
    }

    public int getParticleSpeed() {
        // Slower chips so they don't travel too far
        return 1;
    }

    public int getParticleMaxLife() {
        // Enough lifetime to see arc but still short
        return 12;
    }
}
