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

  endCredit = "Hoop — End Credits\n\n\n\n\n\n\n\n\n" +

    "───────────────────────────────\n" +
    "GAME DESIGN & PROGRAMMING\n" +
    "───────────────────────────────\n\n" +
  "Lead Developer & Designer: Hoop\n" +
  "Gameplay Systems, Collision,\n" +
  "Animation, UI & Logic\n" +
    "Story Implementation and Event Handling\n" +
    "Engine Architecture and Optimization\n\n\n" +

    "───────────────────────────────\n" +
    "STORY & CREATIVE DIRECTION\n" +
    "───────────────────────────────\n\n" +
    "Narrative Concept and Dialogue: Hoop\n" +
    "Lore Building and Character Design: Hoop\n" +
    "World Structure and Pacing: Hoop\n\n\n" +

    "───────────────────────────────\n" +
    "DEVELOPMENT ASSISTANCE\n" +
    "───────────────────────────────\n\n" +
  "ChatGPT (OpenAI), Claude (Anthropic)\n" +
  "Brainstorming, Debugging,\n" +
  "Feature Planning, and Writing Support\n" +
  "System Design Consultation\n" +
  "and Creative Feedback\n\n\n" +

    "───────────────────────────────\n" +
    "SPECIAL INSPIRATION\n" +
    "───────────────────────────────\n\n" +
  "RyiSnow — YouTube Channel\n" +
  "For the incredible 'How to Make a 2D Game in Java'\n" +
  "tutorial series,\n" +
  "which inspired and guided the development of\n" +
  "this entire project.\n" +
    "Thank you for sharing your knowledge and passion.\n\n\n" +

    "───────────────────────────────\n" +
    "ART & VISUAL ASSETS\n" +
    "───────────────────────────────\n\n" +
  "Original and modified assets\n" +
  "from talented creators on itch.io\n" +
    "Environmental Tiles, Characters, Items, and FX\n" +
  "Edited and customized using Piskel\n" +
  "by Hoop Studio\n\n\n" +

    "───────────────────────────────\n" +
    "MUSIC & SOUND EFFECTS\n" +
    "───────────────────────────────\n\n" +
  "Royalty-free background music\n" +
  "and effects from Pixabay\n" +
  "Additional sound editing and mastering\n" +
  "by Hoop Studio\n" +
    "Enhanced ambient effects for immersion\n\n\n" +

    "───────────────────────────────\n" +
    "DEVELOPMENT TOOLS\n" +
    "───────────────────────────────\n\n" +
  "Visual Studio Code — Main\n" +
  "Development Environment\n" +
    "Piskel — Sprite & Animation Editor\n" +
  "Aseprite (Trial) — Color Palette\n" +
  "and Frame Preview\n" +
  "Git & GitHub — Version Control\n" +
  "and Backup\n" +
  "IntelliJ IDEA — Resource Management\n" +
  "& Testing\n\n\n" +

    "───────────────────────────────\n" +
    "PLAYTESTING & FEEDBACK\n" +
    "───────────────────────────────\n\n" +
  "Special thanks to everyone\n" +
  "who tested early builds,\n" +
  "shared feedback, and reported bugs\n" +
  "that shaped the final release.\n\n\n" +

    "───────────────────────────────\n" +
    "SPECIAL THANKS\n" +
    "───────────────────────────────\n\n" +
    "- The Indie Dev Community for \nopen resources & tutorials\n" +
    "- Friends & Family for \nconstant encouragement\n" +
    "- Fellow developers who keep \ncreating despite challenges\n" +
    "- And you — the player — for \ncompleting this journey\n\n\n" +

    "───────────────────────────────\n" +
    "DEDICATION\n" +
    "───────────────────────────────\n\n" +
  "Dedicated to every creator who dreams, codes,\n" +
  "and builds worlds\n" +
    "from nothing but imagination and persistence.\n" +
  "Every frame, every sound, every line of \ncode —" +
  "born from passion.\n\n\n" +

    "───────────────────────────────\n" +
    "EPILOGUE\n" +
    "───────────────────────────────\n\n" +
    "With the Ancient Crystal now glowing once more,\n" +
  "the darkness that once shrouded the land\n" +
  "fades into memory.\n" +
    "Peace has returned... for now.\n\n" +
    "Yet somewhere in the distance,\n" +
    "a new shadow begins to stir.\n\n" +
    "Aiden’s journey is far from over.\n" +
  "Every ending is simply the start of\n" +
  "another tale.\n\n\n" +

    "───────────────────────────────\n" +
    "THANK YOU\n" +
    "───────────────────────────────\n\n" +
  "From all of us at Hoop Studio —\n" +
  "thank you for playing.\n" +
  "Your time, curiosity, and support make this\n" +
  "world possible.\n\n" +
    "See you in the next adventure.\n\n" +
    "— Hoop Studio —\n\n" +
    "© 2025 All Rights Reserved\n";

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

      // Play sound effect
      gp.playSoundEffect(20);
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

      String text = "With the Ancient Crystal in your grasp,\n" +
          "the darkness that once plagued the land fades away.\n\n" +
          "Peace has returned... for now.\n" +
          "But the journey of Aiden is far from over.\n" +
          "Now, a new adventure awaits beyond the horizon.";
      drawString(alpha, 38f, gp.tileSize * 3, text, 70);

      if (counterReached(600)) {
        gp.playMusicOnce(19); // Play ending music once, no loop
        scenePhase++;
      }
    }

    if (scenePhase == 6) {
      drawBlackBackground(1f);

      drawString(1f, 120f, gp.screenHeight / 2, "Aiden Adventure", 40);
      if (counterReached(480)) {
        scenePhase++;
      }
    }

    if (scenePhase == 7) {
      drawBlackBackground(1f);

      y = gp.screenHeight / 2;
      drawString(1f, 38f, gp.screenHeight / 2, endCredit, 40);

      if (counterReached(480)) {
        scenePhase++;
      }
    }

    if (scenePhase == 8) {
      drawBlackBackground(1f);

      // Scroll text upwards
      y -= 1;
      drawString(1f, 38f, y, endCredit, 40);
      if (counterReached(6650)) {scenePhase++;}
    }

    if (scenePhase == 9) {
      // Reset everything
      drawBlackBackground(1f);
      gp.stopMusic();
      scenePhase = 0;
      sceneNum = 0;
      gp.gameState = gp.titleState;
      gp.ui.npc = null;
      gp.player.setDefaultPositions();      
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

    java.awt.FontMetrics fm = g2.getFontMetrics();
    // Only honor explicit newlines; do not auto-wrap by spaces
    String[] lines = text.split("\n", -1);
    for (String line : lines) {
      if (line.isEmpty()) {
        y += lineHeight;
        continue;
      }
      int textWidth = fm.stringWidth(line);
      int x = (gp.screenWidth - textWidth) / 2;
      g2.drawString(line, x, y);
      y += lineHeight;
    }
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

  }
}