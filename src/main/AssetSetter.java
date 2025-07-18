package src.main;

import src.entity.NPC_OldMan;

public class AssetSetter {
    
    GamePanel gp; // Reference to the GamePanel for accessing game settings

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        
    }

    public void setNPC() {
        // NPC 1 - Priest
        gp.npc[0] = new NPC_OldMan(gp);
        gp.npc[0].worldX = gp.tileSize * 21; // 1 tile to the right of player
        gp.npc[0].worldY = gp.tileSize * 21; // Same Y as player
    }
}
