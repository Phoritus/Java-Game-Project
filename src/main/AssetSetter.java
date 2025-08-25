package src.main;

import src.entity.NPC_OldMan;
import src.monster.MonBlueSlime;
import src.object.OBJ_Axe;
import src.object.OBJ_Boot;
import src.object.OBJ_Key;
import src.object.OBJ_Potion;
import src.object.OBJ_Demon_shield;

public class AssetSetter {
    
    GamePanel gp; // Reference to the GamePanel for accessing game settings

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        int i = 0;
        gp.obj[i] = new OBJ_Key(gp);
        gp.obj[i].worldX = gp.tileSize * 25; // Position the key in the world
        gp.obj[i].worldY = gp.tileSize * 19; // Position the key in the world
        i++;
        gp.obj[i] = new OBJ_Key(gp);
        gp.obj[i].worldX = gp.tileSize * 25; // Position the boot in the world
        gp.obj[i].worldY = gp.tileSize * 23; // Position the boot in the world
        i++;
        gp.obj[i] = new OBJ_Key(gp);
        gp.obj[i].worldX = gp.tileSize * 21; // Position the boot in the world
        gp.obj[i].worldY = gp.tileSize * 19; // Position the boot in the world
        i++;
        gp.obj[i] = new OBJ_Axe(gp);
        gp.obj[i].worldX = gp.tileSize * 33; // Position the axe in the world
        gp.obj[i].worldY = gp.tileSize * 21; // Position the axe in the world
        i++;
        gp.obj[i] = new OBJ_Demon_shield(gp);
        gp.obj[i].worldX = gp.tileSize * 36; // Position the demon shield in the world
        gp.obj[i].worldY = gp.tileSize * 25; // Position the demon shield in the world
        i++;
        gp.obj[i] = new OBJ_Potion(gp);
        gp.obj[i].worldX = gp.tileSize * 22; // Position the potion in the world
        gp.obj[i].worldY = gp.tileSize * 21; // Position the potion in the world
        i++;
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
