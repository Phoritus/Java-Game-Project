package src.main;

import src.entity.NPC_OldMan;
import src.monster.MonBlueSlime;
import src.object.OBJ_Boot;

public class AssetSetter {
    
    GamePanel gp; // Reference to the GamePanel for accessing game settings

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        gp.obj[0] = new OBJ_Boot(gp);
        gp.obj[0].worldX = gp.tileSize * 25; // Position the boot in the world
        gp.obj[0].worldY = gp.tileSize * 19; // Position the boot in the world
    }

    public void setNPC() {
        // NPC 1 - Priest
        gp.npc[0] = new NPC_OldMan(gp);
        gp.npc[0].worldX = gp.tileSize * 24; // 1 tile to the right of player
        gp.npc[0].worldY = gp.tileSize * 23; // Same Y as player
    }
    
    public void setMonster() {

        int i = 0;
        // Monster 1 - Blue Slime
        gp.monster[i] = new MonBlueSlime(gp);
        gp.monster[i].worldX = gp.tileSize * 21; // Set initial position
        gp.monster[i].worldY = gp.tileSize * 37; // Set initial position

        // Additional monsters can be added here
        i++;
        gp.monster[i] = new MonBlueSlime(gp);
        gp.monster[i].worldX = gp.tileSize * 23; // Set initial position
        gp.monster[i].worldY = gp.tileSize * 37; // Set initial position

        i++;
        gp.monster[i] = new MonBlueSlime(gp);
        gp.monster[i].worldX = gp.tileSize * 34; // Set initial position
        gp.monster[i].worldY = gp.tileSize * 42; // Set initial position

        i++;
        gp.monster[i] = new MonBlueSlime(gp);
        gp.monster[i].worldX = gp.tileSize * 38; // Set initial position
        gp.monster[i].worldY = gp.tileSize * 42; // Set initial position
    }
}
