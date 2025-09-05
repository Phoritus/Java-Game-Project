package src.object;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_House extends Entity {
    private BufferedImage rawImage; // original pixels
    private BufferedImage scaledImage; // scaled to match tile scaling (16->tileSize)
    private int drawW; // pixels on screen
    private int drawH; // pixels on screen
    // Doorway opening (relative to house top-left in draw space)
    private int doorX, doorY, doorW, doorH;

    public OBJ_House(GamePanel gp) {
        super(gp);
        name = "House";
        type = TYPE_HOUSE; // not pickable
        collision = true;

        try {
            // Load the house sprite (original, e.g., 72x95)
            rawImage = ImageIO.read(getClass().getResourceAsStream("/res/tiles/house.png"));
            // Scale factor based on logical base 16px -> tileSize
            double s = (double) gp.tileSize / 16.0; // e.g., 48/16 = 3x
            drawW = (int) Math.round(rawImage.getWidth() * s);
            drawH = (int) Math.round(rawImage.getHeight() * s);

            // Optional: pre-scale once
            java.awt.Image tmp = rawImage.getScaledInstance(drawW, drawH, java.awt.Image.SCALE_DEFAULT);
            scaledImage = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = scaledImage.createGraphics();
            g.drawImage(tmp, 0, 0, null);
            g.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Collision: approximate only the yellow front edge line (thin, across the
        // base)
        // Use a slim horizontal strip about one tile above the bottom of the sprite,
        // spanning most of the width but leaving small margins on left/right.
        int baseThickness = Math.max(6, gp.tileSize / 6); // ~10px when tileSize=48
        int offsetUp = Math.max(gp.tileSize / 2, (int) Math.round(gp.tileSize * 1)); // lift from image bottom
        int padX = (int) Math.round(drawW * 0.08); // small side margins
        this.solidArea.x = padX;
        this.solidArea.y = Math.max(0, drawH - offsetUp - baseThickness);
        this.solidArea.width = Math.max(1, drawW - padX * 2);
        this.solidArea.height = baseThickness;
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;

        // Approximate door opening location and size relative to the sprite
        // Center slightly to the right; width around ~0.9 tile; height covering the
        // base strip and a bit above
        this.doorW = Math.max((int) Math.round(gp.tileSize * 0.9), (int) Math.round(drawW * 0.18));
        int doorCenterX = (int) Math.round(drawW * 0.62); // tweak if needed to match art
        this.doorX = Math.max(0, doorCenterX - doorW / 2);
        this.doorH = baseThickness + Math.max(8, gp.tileSize / 2);
        this.doorY = Math.max(0, this.solidArea.y - Math.max(4, gp.tileSize / 6));
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Only draw if in view
        if (worldX + drawW > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + drawH > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            // Soft ground shadow to blend
            int shadowY = screenY + Math.max(0, drawH - gp.tileSize);
            int shadowH = Math.max(6, gp.tileSize / 2);
            int shadowX = screenX + (int) (drawW * 0.05);
            int shadowW = (int) (drawW * 0.90);
            java.awt.Color old = g2.getColor();
            g2.setColor(new java.awt.Color(0, 0, 0, 50));
            g2.fillRoundRect(shadowX, shadowY, shadowW, shadowH, shadowH, shadowH);
            g2.setColor(new java.awt.Color(0, 0, 0, 25));
            g2.fillRoundRect(shadowX - 2, shadowY + 2, shadowW + 4, shadowH, shadowH, shadowH);
            g2.setColor(old);
            if (scaledImage != null) {
                g2.drawImage(scaledImage, screenX, screenY, drawW, drawH, null);
            }

            
        }
    }

    // World-space doorway hit check
    public boolean isPointInDoorGapWorld(int pointWorldX, int pointWorldY) {
        int relX = pointWorldX - this.worldX; // relative to top-left of house sprite
        int relY = pointWorldY - this.worldY;
        return relX >= doorX && relX <= doorX + doorW && relY >= doorY && relY <= doorY + doorH;
    }
}
