package src.main;

import src.entity.NPC_Merchant;
import src.entity.NPC_OldMan;
import src.monster.MonBlueSlime;
import src.object.OBJ_Axe;
import src.object.OBJ_Bed;
import src.object.OBJ_Boot;
import src.object.OBJ_Coin;
import src.object.OBJ_Key;
import src.object.OBJ_Lantern;
import src.object.OBJ_ManaCrystal;
import src.object.OBJ_Potion;
import src.tiles_interactive.IT_DryTree;
import src.object.OBJ_Demon_shield;
import src.object.OBJ_Doors;
import src.object.OBJ_House;

public class AssetSetter {

    GamePanel gp; // Reference to the GamePanel for accessing game settings

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        int i = 0;
        int mapnum = 0;
        // Offset for 100x100 map: add 24 to X, 25 to Y
        int offsetX = 24;
        int offsetY = 25;
        
        gp.obj[mapnum][i] = new OBJ_Coin(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (11 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (11 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Coin(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (9 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (9 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Lantern(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (18 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (20 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Key(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (14 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (34 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Axe(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (33 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (7 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Demon_shield(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (30 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (28 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Potion(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (9 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (26 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_ManaCrystal(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (11 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (8 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Boot(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (20 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (12 + offsetY);
        i++;
        gp.obj[mapnum][i] = new OBJ_Doors(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (10 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (12 + offsetY);
        i++;
        // Big house (72x95 original) will be scaled to match tile size automatically
        gp.obj[mapnum][i] = new OBJ_House(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (11 + offsetX);
        gp.obj[mapnum][i].worldY = gp.tileSize * (35 + offsetY);

        mapnum = 1; // Switch to map 1
        i = 0; // Reset index for new map
        gp.obj[mapnum][i] = new OBJ_Bed(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * (39);
        gp.obj[mapnum][i].worldY = gp.tileSize * (34);
    }

    public void setNPC() {
        int mapnum = 0;
        int i = 0;
        // Offset for 100x100 map
        int offsetX = 24;
        int offsetY = 25;
        
        // NPC 1 - Priest
        gp.npc[mapnum][i] = new NPC_OldMan(gp);
        gp.npc[mapnum][i].worldX = gp.tileSize * (24 + offsetX);
        gp.npc[mapnum][i].worldY = gp.tileSize * (23 + offsetY);

        mapnum = 1;
        i = 0; // reset index for a new map layer
        gp.npc[mapnum][i] = new NPC_Merchant(gp);
        gp.npc[mapnum][i].worldX = gp.tileSize * (12 + offsetX);
        gp.npc[mapnum][i].worldY = gp.tileSize * (8 + offsetY);
    }

    public void setMonster() {

        int i = 0;
        int mapnum = 0;
        // Offset for 100x100 map
        int offsetX = 24;
        int offsetY = 25;
        
        // Monster 1 - Blue Slime
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * (11 + offsetX);
        gp.monster[mapnum][i].worldY = gp.tileSize * (33 + offsetY);

        // Additional monsters can be added here
        i++;
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * (10 + offsetX);
        gp.monster[mapnum][i].worldY = gp.tileSize * (18 + offsetY);

        i++;
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * (34 + offsetX);
        gp.monster[mapnum][i].worldY = gp.tileSize * (42 + offsetY);

        i++;
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * (38 + offsetX);
        gp.monster[mapnum][i].worldY = gp.tileSize * (42 + offsetY);
    }

    public void setInteractiveTile() {
        int i = 0;
        int mapnum = 0;
        // Offset for 100x100 map
        int offsetX = 24;
        int offsetY = 25;
        
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 27 + offsetX, 12 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 28 + offsetX, 12 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 29 + offsetX, 12 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 30 + offsetX, 12 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 31 + offsetX, 12 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 32 + offsetX, 12 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 33 + offsetX, 12 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 29 + offsetX, 21 + offsetY);

        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 31 + offsetX, 29 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 32 + offsetX, 29 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 33 + offsetX, 29 + offsetY);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 34 + offsetX, 29 + offsetY);

    }
}
