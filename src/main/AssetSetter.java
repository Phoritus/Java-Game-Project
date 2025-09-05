package src.main;

import src.entity.NPC_Merchant;
import src.entity.NPC_OldMan;
import src.monster.MonBlueSlime;
import src.object.OBJ_Axe;
import src.object.OBJ_Boot;
import src.object.OBJ_Coin;
import src.object.OBJ_Key;
import src.object.OBJ_ManaCrystal;
import src.object.OBJ_Potion;
import src.tiles_interactive.IT_DryTree;
import src.object.OBJ_Demon_shield;
import src.object.OBJ_Doors;
import src.object.OBJ_Heart;
import src.object.OBJ_House;

public class AssetSetter {

    GamePanel gp; // Reference to the GamePanel for accessing game settings

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        int i = 0;
        int mapnum = 0;
        gp.obj[mapnum][i] = new OBJ_Coin(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 11; // Position the coin in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 11; // Position the coin in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_Coin(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 9; // Position the coin in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 9; // Position the coin in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_Key(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 14; // Position the boot in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 34; // Position the boot in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_Axe(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 33; // Position the axe in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 7; // Position the axe in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_Demon_shield(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 30; // Position the demon shield in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 28; // Position the demon shield in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_Potion(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 9; // Position the potion in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 26; // Position the potion in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_ManaCrystal(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 11; // Position the mana crystal in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 8; // Position the mana crystal in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_Boot(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 20; // Position the boot in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 12; // Position the boot in the world
        i++;
        gp.obj[mapnum][i] = new OBJ_Doors(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 10; // Position the doors in the world
        gp.obj[mapnum][i].worldY = gp.tileSize * 12; // Position the doors in the world
        i++;
        // Big house (72x95 original) will be scaled to match tile size automatically
        gp.obj[mapnum][i] = new OBJ_House(gp);
        gp.obj[mapnum][i].worldX = gp.tileSize * 11;
        gp.obj[mapnum][i].worldY = gp.tileSize * 35; // align top-left; adjust as desired
    }

    public void setNPC() {
        int mapnum = 0;
        int i = 0;
        // NPC 1 - Priest
        gp.npc[mapnum][i] = new NPC_OldMan(gp);
        gp.npc[mapnum][i].worldX = gp.tileSize * 24; // 1 tile to the right of player
        gp.npc[mapnum][i].worldY = gp.tileSize * 23; // Same Y as player

        mapnum = 1;
        i = 0; // reset index for a new map layer
        gp.npc[mapnum][i] = new NPC_Merchant(gp);
        gp.npc[mapnum][i].worldX = gp.tileSize * 12; // 1 tile to the right of player
        gp.npc[mapnum][i].worldY = gp.tileSize * 8; // Same Y as player
    }

    public void setMonster() {

        int i = 0;
        int mapnum = 0;
        // Monster 1 - Blue Slime
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * 11; // Set initial position
        gp.monster[mapnum][i].worldY = gp.tileSize * 33; // Set initial position

        // Additional monsters can be added here
        i++;
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * 10; // Set initial position
        gp.monster[mapnum][i].worldY = gp.tileSize * 18; // Set initial position

        i++;
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * 34; // Set initial position
        gp.monster[mapnum][i].worldY = gp.tileSize * 42; // Set initial position

        i++;
        gp.monster[mapnum][i] = new MonBlueSlime(gp);
        gp.monster[mapnum][i].worldX = gp.tileSize * 38; // Set initial position
        gp.monster[mapnum][i].worldY = gp.tileSize * 42; // Set initial position
    }

    public void setInteractiveTile() {
        int i = 0;
        int mapnum = 0;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 27, 12);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 28, 12);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 29, 12);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 30, 12);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 31, 12);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 32, 12);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 33, 12);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 29, 21);

        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 31, 29);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 32, 29);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 33, 29);
        i++;
        gp.iTile[mapnum][i] = new IT_DryTree(gp, 34, 29);

    }
}
