package src.main;

import src.entity.NPC_OldMan;
import src.object.OBJ_Boot;
import src.object.OBJ_Chest;
import src.object.OBJ_Doors;

public class AssetSetter {
    
    GamePanel gp; // Reference to the GamePanel for accessing game settings

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        gp.obj[0] = new OBJ_Doors(gp); // Create a new door object
        gp.obj[0].worldX = gp.tileSize * 21; // Set the X position
        gp.obj[0].worldY = gp.tileSize * 25; // Set the Y position

        gp.obj[1] = new OBJ_Doors(gp); // Create a new door object
        gp.obj[1].worldX = gp.tileSize * 23; // Set the X position
        gp.obj[1].worldY = gp.tileSize * 21; // Set the Y position

        gp.obj[2] = new OBJ_Boot(gp); // Create a new boot object
        gp.obj[2].worldX = gp.tileSize * 23; // Set the X position (visible on screen)
        gp.obj[2].worldY = gp.tileSize * 23; // Set the Y position (visible on screen)

        gp.obj[3] = new OBJ_Chest(gp); // Create a new chest object
        gp.obj[3].worldX = gp.tileSize * 25; // Set the X position (visible on screen)
        gp.obj[3].worldY = gp.tileSize * 30; // Set the Y position (visible on screen)


    }

    public void setNPC() {
        // NPC 1 - Priest
        gp.npc[0] = new NPC_OldMan(gp);
        gp.npc[0].worldX = gp.tileSize * 21; // 1 tile to the right of player
        gp.npc[0].worldY = gp.tileSize * 21; // Same Y as player

        // NPC 1 - Priest
        gp.npc[1] = new NPC_OldMan(gp);
        gp.npc[1].worldX = gp.tileSize * 21; // 1 tile to the right of player
        gp.npc[1].worldY = gp.tileSize * 25; // Same Y as player

        // NPC 1 - Priest
        gp.npc[2] = new NPC_OldMan(gp);
        gp.npc[2].worldX = gp.tileSize * 21; // 1 tile to the right of player
        gp.npc[2].worldY = gp.tileSize * 30; // Same Y as player
    }
}
