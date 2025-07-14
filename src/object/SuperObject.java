package src.object;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.text.Utilities;

import src.main.GamePanel;

public class SuperObject {
    public BufferedImage image;
    public BufferedImage[] animationFrames; // Array for animation frames
    public String name;
    public boolean collision = false;
    public int worldX, worldY; // World coordinates of the object
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48); // Default size for collision detection
    public int solidAreaDefaultX = 0, solidAreaDefaultY = 0; // Default position of the solid area
    Utilities  uTool = new Utilities(); // Utility class for image scaling

    // Animation variables
    public int animationFrame = 0;
    public int animationCounter = 0;
    public int animationSpeed = 15; // How fast animation plays (higher = slower)
    public boolean hasAnimation = false;

    public void draw (Graphics2D g2, GamePanel gp) {
        
        int ScreenX = worldX - gp.player.worldX + gp.player.screenX;
        int ScreenY = worldY - gp.player.worldY + gp.player.screenY;

        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            
            // Update animation if object has animation
            if (hasAnimation && animationFrames != null) {
                // Special handling for chest animation
                if (name != null && name.equals("Chest")) {
                    // Cast to OBJ_Chest and use its custom animation update
                    if (this instanceof src.object.OBJ_Chest) {
                        src.object.OBJ_Chest chest = (src.object.OBJ_Chest) this;
                        chest.updateAnimation();
                    }
                } else {
                    // Standard animation for other objects
                    animationCounter++;
                    if (animationCounter > animationSpeed) {
                        animationCounter = 0;
                        animationFrame++;
                        if (animationFrame >= animationFrames.length) {
                            animationFrame = 0;
                        }
                    }
                }
            }
            
            // Draw background that matches the surrounding area
            // Get the tile type at this position
            int tileCol = worldX / gp.tileSize;
            int tileRow = worldY / gp.tileSize;
            
            if (tileCol >= 0 && tileCol < gp.maxWorldCol && tileRow >= 0 && tileRow < gp.maxWorldRow) {
                int tileNum = gp.tileManager.mapTileNum[tileCol][tileRow];
                
                // Draw the same tile as background
                if (tileNum >= 0 && tileNum < gp.tileManager.tile.length && gp.tileManager.tile[tileNum] != null) {
                    g2.drawImage(gp.tileManager.tile[tileNum].image, ScreenX, ScreenY, gp.tileSize, gp.tileSize, null);
                } else {
                    // Fallback to grass if tile not found
                    try {
                        BufferedImage grassImage = ImageIO.read(getClass().getResourceAsStream("/res/tiles/grass1.png"));
                        g2.drawImage(grassImage, ScreenX, ScreenY, gp.tileSize, gp.tileSize, null);
                    } catch (Exception e) {
                        g2.setColor(Color.GREEN);
                        g2.fillRect(ScreenX, ScreenY, gp.tileSize, gp.tileSize);
                    }
                }
            } else {
                // If position is out of bounds, use grass as default
                try {
                    BufferedImage grassImage = ImageIO.read(getClass().getResourceAsStream("/res/tiles/grass1.png"));
                    g2.drawImage(grassImage, ScreenX, ScreenY, gp.tileSize, gp.tileSize, null);
                } catch (Exception e) {
                    g2.setColor(Color.GREEN);
                    g2.fillRect(ScreenX, ScreenY, gp.tileSize, gp.tileSize);
                }
            }
            
            // Draw the object on top with different sizes based on object type
            int objectSize;
            if (name != null && (name.equals("Door") || name.equals("Chest"))) {
                objectSize = (int)(gp.tileSize * 1.0); // Normal size for doors and chests
            } else if (name != null && name.equals("Key")) {
                objectSize = (int)(gp.tileSize * 0.9); // Smaller size for keys
            } else if (name != null && name.equals("Boot")) {
                objectSize = (int)(gp.tileSize * 0.65); // Smaller size for boots
            } else {
                objectSize = (int)(gp.tileSize * 1.3); // Medium size for other objects like boots
            }
            
            int offsetX = (gp.tileSize - objectSize) / 2; // Center the object
            int offsetY = (gp.tileSize - objectSize) / 2;
            
            // Choose which image to draw based on animation
            BufferedImage currentImage = image; // Default to static image
            if (hasAnimation && animationFrames != null && animationFrames.length > 0) {
                // Special handling for chest
                if (name != null && name.equals("Chest") && this instanceof src.object.OBJ_Chest) {
                    src.object.OBJ_Chest chest = (src.object.OBJ_Chest) this;
                    currentImage = chest.getCurrentFrame();
                } else {
                    // Standard animation for other objects
                    currentImage = animationFrames[animationFrame];
                }
            }
            
            g2.drawImage(currentImage, ScreenX + offsetX, ScreenY + offsetY, objectSize, objectSize, null);
        }
    }
}