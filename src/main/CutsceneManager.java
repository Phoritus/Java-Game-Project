package src.main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import src.entity.PlayerDummy;
import src.monster.MonMinotour;
import src.object.OBJ_Cyrstal;
import src.object.OBJ_IronDoor;

public class CutsceneManager {
  GamePanel gp;
  Graphics2D g2;
  public int sceneNum = 0;
  public int scenePhase = 0;
  public int counter = 0;
  float alpha = 0f;
  int y;
  String endCredit;

  // Scene Number
  public final int NA = 0;
  public final int minotour = 1;
  public final int ending = 2;

  public CutsceneManager(GamePanel gp) {
    this.gp = gp;

    endCredit = "Programming & Design\n" +
                "Hoop\n\n\n\n\n\n" +
                "by ChatGPT & OpenAI\n\n" +
                "Art & Assets\n" +
                "by itch.io\n\n" +
                "Music & Sound Effects\n" +
                "by Pixabay.com\n\n" +
                "Thank you for playing!";
  }

  public void draw(Graphics2D g2) {
    this.g2 = g2;

    switch (sceneNum) {
      case minotour:
        scene_minotour();
        break;
      case ending:
        scene_ending();
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

  public void scene_ending() {
    if (scenePhase == 0) {

      gp.stopMusic();
      gp.ui.npc = new OBJ_Cyrstal(gp);
      // Start dialogue properly so F advances lines
      gp.ui.npc.startDialogue(gp.ui.npc, 0); // sets gameState = dialogueState
      scenePhase++;
    }

    if (scenePhase == 1) {
      // While dialogue is open, let UI handle drawing/advancement.
      // When dialogue closes (gameState changes), advance the scene.
      if (gp.gameState != gp.dialogueState) {
        scenePhase++;
      }
    }

    if (scenePhase == 2) {
      // Enter dedicated cutscene state so HUD (hearts/mana) doesn't draw
      gp.gameState = gp.cutsceneState;
      gp.playSoundEffect(4);
      scenePhase++;
    }

    if (scenePhase == 3) {
      // Wait until the sound effect is done
      if (counterReached(300)) { // Assuming 300 frames is enough
        scenePhase++;
      }

    }

    if (scenePhase == 4) {
      // Fade out to black
      alpha += 0.005f;
      if (alpha > 1f) {
        alpha = 1f;
      }
      drawBlackBackground(alpha);

      if (alpha >= 1f) {
        alpha = 0f;
        scenePhase++;
      }
    }

    if (scenePhase == 5) {
      // Reset game state
      drawBlackBackground(1f);

      alpha += 0.005f;
      if (alpha > 1f) {
        alpha = 1f;
      }

      String text =
        "With the Ancient Crystal in your grasp,\n" +
        "the darkness that once plagued the land fades away.\n\n" +
        "Peace has returned... for now.\n\n" +
        "But the journey of Aiden is far from over.\n\n" +
        "Now, a new adventure awaits beyond the horizon.";
      drawString(alpha, 38f, gp.tileSize * 3, text, 70);

      if (counterReached(600)) {
        gp.playMusic(0);
        scenePhase++;
      }
    }

    if (scenePhase == 6) {
      drawBlackBackground(1f);

      drawString(.1f, 120f, gp.screenHeight/2, "Aiden Adventure", 40);
      if (counterReached(480)) {
        scenePhase++;
      }
    }

    if (scenePhase == 7) {
        drawBlackBackground(1f);

        y = gp.screenHeight / 2;
        drawString(1f, 38f, gp.screenHeight/2, endCredit, 40);

        if (counterReached(480)) {
          scenePhase++;
        }
    }

    if (scenePhase == 8) {
      drawBlackBackground(1f);

      // Scroll text upwards
      y -= 1;
      drawString(1f, 38f, y, endCredit, 40);
    }

  }

  public boolean counterReached(int target) {

    counter++;
    if (counter >= target) {
      counter = 0;
      return true;
    }
    return false;
  }

  public void drawBlackBackground(float alpha) {
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
  }

  public void drawString(float alpha, float fontSize, int y, String text, int lineHeight) {
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g2.setColor(Color.WHITE);
    g2.setFont(g2.getFont().deriveFont(fontSize));
    
    for (String line : text.split("\n")) {
      // Center text based on current screen size and current font metrics
      java.awt.FontMetrics fm = g2.getFontMetrics();
      int textWidth = fm.stringWidth(line);
      int x = (gp.screenWidth - textWidth) / 2;
      g2.drawString(line, x, y);
      y += lineHeight; // Move to next line position
    }
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

  }
}