package src.object;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import src.entity.Entity;
import src.entity.Player;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Boot extends Entity {
    UtilityTool uTool = new UtilityTool(); // Utility tool for image scaling
    private OBJ_Animation animation; // Animation controller
    
    public OBJ_Boot(GamePanel gp) {
        super(gp);
        name = "Boot";
        // Boots should not change speed when spawned; apply on pickup via use().
        type = TYPE_PICKUP_ONLY;

        // Load animation frames
        BufferedImage[] bootFrames = new BufferedImage[2];
        
        try {
            // Load boot animation frames
            bootFrames[0] = setup("/res/objects/boots/boot.png");
            bootFrames[1] = setup("/res/objects/boots/boot1.png");

            // Create animation controller with frames and speed (20 = slower animation)
            animation = new OBJ_Animation(bootFrames, 20);
            
            // Set the default image to the first frame
            image = bootFrames[0];
            
        } catch (Exception e) {
            e.printStackTrace();          
            
        }
    }
    
    // Update method to handle animation
    public void update() {
        if (animation != null) {
            animation.update();
            BufferedImage newImage = animation.getCurrentFrame();
            if (newImage != null) {
                image = newImage;
            }
        }
        super.update(); // Call parent update if exists
    }
    
    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Check if boot is within screen bounds
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            
            // Calculate screen position
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            
            // Draw the boot image directly (bypass Entity's direction-based animation)
            if (image != null) {

                int bootSize = gp.tileSize - 20;
                int offsetX = (gp.tileSize - bootSize) / 2; // จัดให้อยู่กลาง tile
                int offsetY = (gp.tileSize - bootSize) / 2;
                
                g2.drawImage(image, screenX + offsetX, screenY + offsetY, bootSize, bootSize, null);
            }
            
            // Debug rectangle (green for boot)
            // g2.setColor(Color.GREEN);
            // g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
            
            // System.out.println("Boot drawn at screen: " + screenX + ", " + screenY + 
            //                  ", world: " + worldX + ", " + worldY);
        }
    }
    
    // Method to pause/resume animation
    public void setAnimating(boolean animating) {
        if (animation != null) {
            animation.setAnimating(animating);
        }
    }
    
    // Method to reset animation
    public void resetAnimation() {
        if (animation != null) {
            animation.reset();
        }
    }

    @Override
    public void use(Entity entity) {
        // When picked up by the player, grant a speed bonus once
        if (entity instanceof Player) {
            Player p = (Player) entity;
            if (!p.hasBoots) {
                p.speedBonus += 2;
                p.hasBoots = true;
                gp.playSoundEffect(2); // boots speed SFX
                gp.ui.addMessage("Speed up!");
            } else {
                // Already applied; ignore duplicates
            }
        }
    }
}
