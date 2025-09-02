package src.entity;

import src.main.GamePanel;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Projectile extends Entity {
    Entity user; // The entity that fired the projectile
    // Directional spawn offsets (pixels) index: 0=up,1=down,2=left,3=right
    public int[] spawnOffsetX = new int[] {0, 0, 0, 0};
    public int[] spawnOffsetY = new int[] {0, 0, 0, 0};
    // Visual scale for drawing (1.0 = tile size). Override per projectile type.
    public float drawScale = 1.0f;

    public Projectile(GamePanel gp) {
        super(gp);
    // Centered, compact hitbox for projectiles
    int size = Math.max(10, gp.tileSize / 4); // ~12px on 48px tiles
    int offset = (gp.tileSize - size) / 2;
    this.solidArea = new java.awt.Rectangle(offset, offset, size, size);
    this.solidAreaDefaultX = this.solidArea.x;
    this.solidAreaDefaultY = this.solidArea.y;
    }

    public void set(int worldX, int worldY, String direction, boolean alive, Entity user) {
        this.direction = direction;
        this.alive = alive;
        this.life = this.maxLife;
        this.user = user;
        int idx;
        switch (direction == null ? "right" : direction) {
            case "up": idx = 0; break;
            case "down": idx = 1; break;
            case "left": idx = 2; break;
            case "right": default: idx = 3; break;
        }
    // Player passes center coordinates; convert to top-left tile origin before applying offsets
    int half = gp.tileSize / 2;
    this.worldX = (worldX - half) + spawnOffsetX[idx];
    this.worldY = (worldY - half) + spawnOffsetY[idx];
    }

    public void update(){
        
        if (user == gp.player) {
            int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
            if (monsterIndex != -1) {
                gp.player.damageMonster(monsterIndex, attack);
                alive = false;
            }
        }
        if (user != gp.player) {
            boolean contactPlayer = gp.cChecker.checkPlayer(this);
            if (contactPlayer && !gp.player.invincible) {
                damagePlayer(attack);
                alive = false;
            }
        }

        if (alive) {
            switch (direction) {
                case "up":
                    worldY -= speed;
                    break;
                case "down":
                    worldY += speed;
                    break;
                case "left":
                    worldX -= speed;
                    break;
                case "right":
                    worldX += speed;
                    break;
            }
            // Despawn if out of world bounds
            if (worldX < 0 || worldX > gp.worldWidth - gp.tileSize ||
                worldY < 0 || worldY > gp.worldHeight - gp.tileSize) {
                alive = false;
            }
        }

        life--;
        if (life <= 0) {
            alive = false;
        }

        // Advance animation across 5 frames
        spriteCounter++;
        if (spriteCounter > 6) { // quicker spin
            spriteCounter = 0;
            spriteNum++;
            if (spriteNum > 5) spriteNum = 1;
        }
    }

    public boolean hasResource(Entity user) {
        boolean haveResource = false;
        return haveResource;
    }

    public void subtractResource(Entity user) {}

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Screen position relative to player
        screenX = worldX - gp.player.worldX + gp.player.screenX;
        screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Only draw if visible on screen
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            BufferedImage frame = null;
            String dir = (direction == null) ? "right" : direction;
            int s = spriteNum;
            if (s < 1 || s > 5) s = 1;

            switch (dir) {
                case "up":
                    frame = (s==1?up1 : s==2?up2 : s==3?up3 : s==4?up4 : up5);
                    break;
                case "down":
                    frame = (s==1?down1 : s==2?down2 : s==3?down3 : s==4?down4 : down5);
                    break;
                case "left":
                    frame = (s==1?left1 : s==2?left2 : s==3?left3 : s==4?left4 : left5);
                    break;
                case "right":
                default:
                    frame = (s==1?right1 : s==2?right2 : s==3?right3 : s==4?right4 : right5);
                    break;
            }

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            if (frame != null) {
                int drawSize = Math.max(1, Math.round(gp.tileSize * drawScale));
                int ox = screenX + (gp.tileSize - drawSize) / 2;
                int oy = screenY + (gp.tileSize - drawSize) / 2;
                g2.drawImage(frame, ox, oy, drawSize, drawSize, null);
            } else {
                // Visible fallback if any frame missing
                g2.setColor(java.awt.Color.ORANGE);
                int sz = Math.max(2, Math.round(gp.tileSize * drawScale / 2f));
                int ox = screenX + (gp.tileSize - sz) / 2;
                int oy = screenY + (gp.tileSize - sz) / 2;
                g2.fillOval(ox, oy, sz, sz);
            }
        }
    }
}
