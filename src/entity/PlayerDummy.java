package src.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;

import src.main.GamePanel;
import src.main.UtilityTool;

public class PlayerDummy extends Entity {

  public static final String npcName = "Dummy";
  private BufferedImage dummyImage; // เก็บแค่รูปเดียว

  public PlayerDummy(GamePanel gp) {
    super(gp);
    name = npcName;
    getPlayerImage();
  }

  public void getPlayerImage() {
    // โหลดแค่รูปเดียว (idle back frame 1)
    dummyImage = setup("res/player/player_idle_back/idle_back1.png");
  }
  
  public BufferedImage setup(String imagePath) {
    BufferedImage image = null;
    try {
      image = ImageIO.read(new java.io.File(imagePath));
      // Scale to tileSize using nearest-neighbor
      UtilityTool uTool = new UtilityTool();
      image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return image;
  }

  @Override
  public void draw(Graphics2D g2, GamePanel gp) {
    // คำนวณตำแหน่งบนหน้าจอ
    int screenX = worldX - gp.player.worldX + gp.player.screenX;
    int screenY = worldY - gp.player.worldY + gp.player.screenY;
    
    if (dummyImage != null) {
      // ใช้ nearest-neighbor interpolation
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      
      // ขยาย 2.5 เท่า (เหมือน Player)
      int scaledSize = (int) (gp.tileSize * 2.5);
      
      // Center sprite
      int centerX = screenX - (scaledSize - gp.tileSize) / 2;
      int centerY = screenY - (scaledSize - gp.tileSize) / 2;
      
      // วาดรูป
      g2.drawImage(dummyImage, centerX, centerY, scaledSize, scaledSize, null);
    }
  }
}