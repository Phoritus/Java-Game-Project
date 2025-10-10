package src.main;

import java.awt.Graphics2D;

import src.entity.PlayerDummy;
import src.monster.MonMinotour;
import src.object.OBJ_IronDoor;
 

public class CutsceneManager {
  GamePanel gp;
  Graphics2D g2;
  public int sceneNum = 0;
  public int scenePhase = 0;

  // Scene Number
  public final int NA = 0;
  public final int minotour = 1;

  public CutsceneManager(GamePanel gp) {
    this.gp = gp;
  }

  public void draw(Graphics2D g2) {
    this.g2 = g2;

    switch (sceneNum) {
      case minotour:
        scene_minotour();
        break;
    }
    
  }

  public void scene_minotour() {
    if (scenePhase == 0) {
      gp.bossBattleOn = true;
      
      // Create iron door
      for (int i = 0; i < gp.obj[1].length; i++) {
        if (gp.obj[gp.currentMap][i] == null) {
          gp.obj[gp.currentMap][i] = new OBJ_IronDoor(gp);
          gp.obj[gp.currentMap][i].worldX = gp.tileSize * 50;
          gp.obj[gp.currentMap][i].worldY = gp.tileSize * 53;
          gp.obj[gp.currentMap][i].temp = true;
          gp.playSoundEffect(3);
          break;
        }
      }

      for (int i = 0; i < gp.npc[1].length; i++) {
        if (gp.npc[gp.currentMap][i] == null) {
          gp.npc[gp.currentMap][i] = new PlayerDummy(gp);
          gp.npc[gp.currentMap][i].worldX = gp.player.worldX;
          gp.npc[gp.currentMap][i].worldY = gp.player.worldY;
          gp.npc[gp.currentMap][i].direction = gp.player.direction;
          break;
        } 
      }

      gp.player.drawing = false; // Hide player during cutscene
      scenePhase++;
    } 
    
    if (scenePhase == 1) {
      // Move PLAYER down (camera will follow automatically)
      gp.player.worldY -= 2;
      
      // Check if player reached target position
      if (gp.player.worldY < gp.tileSize * 44) {
        scenePhase++;
      }
    } 
    
    if (scenePhase == 2) {
      // Find and activate boss
      for (int i = 0; i < gp.monster[1].length; i++) {
        if (gp.monster[gp.currentMap][i] != null && gp.monster[gp.currentMap][i].name == MonMinotour.monName) {
          gp.monster[gp.currentMap][i].sleep = false;
          gp.monster[gp.currentMap][i].startDialogue(gp.monster[gp.currentMap][i], 0);
          scenePhase++;
          break;
        }
      }
      
    } 
    
    if (scenePhase == 3) {
      // Wait for dialogue to finish
      gp.ui.drawDialogueScreen();
      gp.gameState = gp.cutsceneState;
      
    }

    if (scenePhase == 4) {

      for (int i = 0; i < gp.npc[1].length; i++) {
        if (gp.npc[gp.currentMap][i] != null && PlayerDummy.npcName.equals(gp.npc[gp.currentMap][i].name)) {
          gp.player.worldX = gp.npc[gp.currentMap][i].worldX;
          gp.player.worldY = gp.npc[gp.currentMap][i].worldY;
          gp.npc[gp.currentMap][i] = null; // Remove dummy
          break;
        } 
      }
      gp.player.drawing = true; // Show player again
      // Reset
      sceneNum = NA;
      scenePhase = 0;
      gp.gameState = gp.playState;

      // Play boss fight music
      gp.stopMusic();
      gp.playMusic(16);
    }
  }
}
