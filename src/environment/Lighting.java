package src.environment;

import src.main.GamePanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;

public class Lighting {
  GamePanel gp;
  BufferedImage darknessFilter;
  int dayCounts;
  public float filterAlpha = 0f; // Initial alpha value for the darkness filter

  public final int day = 0;
  public final int dusk = 1;
  public final int night = 2;
  public final int dawn = 3;
  public int dayState = day;

  public Lighting(GamePanel gp) {
    this.gp = gp;
    setLightSource();
  }

  public void setLightSource() {
    darknessFilter = new BufferedImage(gp.screenWidth, gp.screenHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) darknessFilter.getGraphics();

    if (gp.player.currentLight == null) {
      g2.setColor(new Color(0, 0, 0, 0.96f));
    } else {
      int centerX = gp.player.screenX + (gp.tileSize / 2);
      int centerY = gp.player.screenY + (gp.tileSize / 2);

      // Create a gradational light effect (optional enhancement)
      Color color[] = new Color[12];
      float fraction[] = new float[12];

      color[0] = new Color(0, 0, 0.1f, 0.1f);
      color[1] = new Color(0, 0, 0.1f, 0.42f);
      color[2] = new Color(0, 0, 0.1f, 0.52f);
      color[3] = new Color(0, 0, 0.1f, 0.61f);
      color[4] = new Color(0, 0, 0.1f, 0.69f);
      color[5] = new Color(0, 0, 0.1f, 0.76f);
      color[6] = new Color(0, 0, 0.1f, 0.85f);
      color[7] = new Color(0, 0, 0.1f, 0.87f);
      color[8] = new Color(0, 0, 0.1f, 0.91f);
      color[9] = new Color(0, 0, 0.1f, 0.94f);
      color[10] = new Color(0, 0, 0.1f, 0.96f);
      color[11] = new Color(0, 0, 0.1f, 0.98f);

      fraction[0] = 0f;
      fraction[1] = 0.4f;
      fraction[2] = 0.5f;
      fraction[3] = 0.6f;
      fraction[4] = 0.65f;
      fraction[5] = 0.75f;
      fraction[6] = 0.85f;
      fraction[7] = 0.87f;
      fraction[8] = 0.91f;
      fraction[9] = 0.94f;
      fraction[10] = 0.96f;
      fraction[11] = 0.98f;

      RadialGradientPaint gPaint = new RadialGradientPaint(centerX, centerY, gp.player.currentLight.lightRadius,
          fraction, color);
      g2.setPaint(gPaint);

    }
    g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
    g2.dispose();
  }

  public void update() {
    if (gp.player.lightUpdated) {
      setLightSource();
      gp.player.lightUpdated = false;
    }

    if (dayState == day) {
      dayCounts++;

      if (dayCounts > 3600) { // After some time, transition to dusk
        dayState = dusk;
        dayCounts = 0;
        filterAlpha = 0f; // Reset alpha for dusk transition
      }
    }

    if (dayState == dusk) {
      filterAlpha += 0.001f; // Gradually increase alpha to darken the screen

      if (filterAlpha > 1f) {
        filterAlpha = 1f;
        dayState = night;
      }
    }
    if (dayState == night) {
      dayCounts++;

      if (dayCounts > 36000) { // After 10 minutes, transition to dawn
        dayState = dawn;
        dayCounts = 0;
      }
    }
    if (dayState == dawn) {
      filterAlpha -= 0.001f; // Gradually decrease alpha to lighten the screen

      if (filterAlpha < 0f) {
        filterAlpha = 0f;
        dayState = day;
      }
    }
  }

  public void draw(Graphics2D g2) {
    // If godMode is on, keep the screen bright (skip dark overlay entirely)
    boolean skipDarkOverlay = gp != null && gp.keyHandler != null && gp.keyHandler.godMode;

    if (!skipDarkOverlay) {
      if (gp.currentArea == gp.outside) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, filterAlpha));
      }

      if (gp.currentArea == gp.outside || gp.currentArea == gp.dungeon) {
        g2.drawImage(darknessFilter, 0, 0, null);
      }

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // Reset alpha to full opacity
    } else {
      // Ensure composite is reset in godMode as well
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // DEBUG
    String situation = "";

    switch (dayState) {
      case day:
        situation = "Day";
        break;
      case dusk:
        situation = "Dusk";
        break;
      case night:
        situation = "Night";
        break;
      case dawn:
        situation = "Dawn";
        break;
    }

    g2.setColor(Color.white);
    g2.setFont(g2.getFont().deriveFont(50f));
    g2.drawString(situation, 800, 500);
  }
}
