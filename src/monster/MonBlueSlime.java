package src.monster;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

import src.entity.Entity;
import src.main.GamePanel;
import src.object.OBJ_Coin;
import src.object.OBJ_Heart;
import src.object.OBJ_ManaCrystal;
import src.object.OBJ_Rock;

public class MonBlueSlime extends Entity {

    private final Random rng = new Random();
    private int localIdleCounter = 0;
    private static final double SCALE = 1.5; // increase slime size
    boolean hpBarOn = false;
    int hpBarCounter = 0;
    private final GamePanel gamePanel; // local reference since Entity.gp is package-private
    // Simple AI: chase player when within a vision radius
    private boolean aggro = false;
    private final int visionRangeTiles = 1; // how far the slime can "see" the player
    private final int chaseSpeed = 2;
    private final int baseSpeed = 1;
    private int loseSightCounter = 0;
    private final int loseSightThreshold = 120; // ~2 seconds until giving up

    public MonBlueSlime(GamePanel gp) {
        super(gp);
        this.gamePanel = gp;

        type = TYPE_MONSTER;
        name = "Blue Slime";
        speed = baseSpeed;
        maxLife = 4;
        life = maxLife;
        attack = 2;
        defense = 1;
        direction = "down";
        exp = 5;
        projectile = new OBJ_Rock(gp);

        // Collision box tuned for a small slime
        solidArea = new Rectangle(6, 24, 36, 20);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        loadImages();
    }

    private void loadImages() {
        // Use movement frames for all directions (0..3)
        String[] walk = new String[] {
                "/res/monster/slime/walk/walk1.png",
                "/res/monster/slime/walk/walk2.png",
                "/res/monster/slime/walk/walk3.png",
                "/res/monster/slime/walk/walk4.png",
                "/res/monster/slime/walk/walk5.png",
                "/res/monster/slime/walk/walk6.png"
        };

        String[] idle = new String[] {
                "/res/monster/slime/idle/idle1.png",
                "/res/monster/slime/idle/idle2.png",
                "/res/monster/slime/idle/idle3.png",
                "/res/monster/slime/idle/idle4.png"
        };

        for (int dir = 0; dir < 4; dir++) {
            for (int i = 0; i < walk.length; i++) {
                animationImages[dir][i] = setup(walk[i]);
            }
        }

        // Idle stored in direction index 4
        for (int i = 0; i < idle.length; i++) {
            animationImages[4][i] = setup(idle[i]);
        }
    }

    @Override
    public void setAction() {
        // If player is within vision, chase aggressively
        if (canSeePlayer()) {
            aggro = true;
            loseSightCounter = 0;
            speed = chaseSpeed;
            steerTowardsPlayer();
            return;
        }

        // If recently aggroed, continue chasing for a short while even if out of sight
        if (aggro) {
            loseSightCounter++;
            if (loseSightCounter < loseSightThreshold) {
                speed = chaseSpeed;
                steerTowardsPlayer();
                return;
            } else {
                // give up
                aggro = false;
                speed = baseSpeed;
            }
        }

        // Default roaming behavior when not chasing
        actionLockCounter++;
        if (actionLockCounter >= 120) { // change every ~2 seconds
            int roll = rng.nextInt(100);
            if (roll < 25)
                direction = "up";
            else if (roll < 50)
                direction = "down";
            else if (roll < 75)
                direction = "left";
            else
                direction = "right";
            // small chance to idle
            if (rng.nextInt(5) == 0)
                direction = "idle";
            actionLockCounter = 0;
        }
        int i = new Random().nextInt(100) + 1;
        if (i > 99 && !projectile.alive && shotAvailableCounter == 30) {
            projectile.set(worldX, worldY, direction, true, this);
            gp.projectileList.add(projectile);
            shotAvailableCounter = 0;
        }
    }

    @Override
    public void update() {
        super.update(); // movement/collision + frameIndex advance

        // If dead/dying, ensure HP bar is hidden
        if (!alive || this.dying) {
            hpBarOn = false;
            hpBarCounter = 0;
        }

        if ("idle".equals(direction)) {
            localIdleCounter++;
            if (localIdleCounter > 10) {
                localIdleCounter = 0;
                idleFrame++;
                if (idleFrame > 4)
                    idleFrame = 1;
            }
        } else {
            localIdleCounter = 0;
            idleFrame = 1;
        }

        // HP bar visibility timer: hide after ~10 seconds if not re-triggered
        if (hpBarOn) {
            hpBarCounter++;
            // Assuming ~60 updates per second
            if (hpBarCounter > 600) {
                hpBarOn = false;
                hpBarCounter = 0;
            }
        }
    }

    // Called when this monster is hit by the player to reveal the HP bar and reset
    // timer
    public void showHpBar() {
        hpBarOn = true;
        hpBarCounter = 0;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Compute on-screen position
        screenX = worldX - gp.player.worldX + gp.player.screenX;
        screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Only draw if visible
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            BufferedImage image = null;
            int directionIndex;
            switch (direction) {
                case "up":
                    directionIndex = 0;
                    break;
                case "down":
                    directionIndex = 1;
                    break;
                case "left":
                    directionIndex = 2;
                    break;
                case "right":
                    directionIndex = 3;
                    break;
                case "idle":
                default:
                    directionIndex = 4;
                    break;
            }

            // Monster HP Bar: draw only when recently hit
            if (hpBarOn) {
                double oneScale = (double) gp.tileSize / maxLife;
                double hpBarValue = oneScale * life;

                g2.setColor(new Color(35, 35, 35));
                g2.fillRect(screenX - 1, screenY - 16, gp.tileSize + 2, 8);

                g2.setColor(new Color(255, 0, 30));
                g2.fillRect(screenX, screenY - 15, (int) hpBarValue, 6);
            }

            // Use the shared animation system's frameIndex (advanced in Entity.update)
            int frame = ("idle".equals(direction)) ? Math.min(idleFrame - 1, 5) : Math.min(this.frameIndex, 5);
            if (animationImages[directionIndex][frame] != null) {
                image = animationImages[directionIndex][frame];
            }

            if (image != null) {
                // Increase slime size and center on its tile
                int scaled = (int) (gp.tileSize * SCALE);
                int offsetX = (gp.tileSize - scaled) / 2;
                int offsetY = (gp.tileSize - scaled) / 2;

                // Visuals: dying fade takes precedence, else invincibility
                Composite old = g2.getComposite();
                if (this.dying) {
                    // Fade OUT over ~40 frames: 1.0 -> 0.0
                    float alpha = 1.0f - Math.min(1.0f, dyingCounter / 40.0f);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.0f, alpha)));
                } else if (this.invincible) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                }
                g2.drawImage(image, screenX + offsetX, screenY + offsetY, scaled, scaled, null);
                g2.setComposite(old);
            }
        }
    }

    public void damageReaction() {
        actionLockCounter = 0;
        aggro = true; // getting hit makes the slime angry
        loseSightCounter = 0;
        speed = chaseSpeed;
        steerTowardsPlayer();
    }

    // --- Simple vision + steering helpers ---
    private boolean canSeePlayer() {
        if (gamePanel == null || gamePanel.player == null)
            return false;
        int vx = (gamePanel.player.worldX + gamePanel.player.solidAreaDefaultX) - (worldX + solidAreaDefaultX);
        int vy = (gamePanel.player.worldY + gamePanel.player.solidAreaDefaultY) - (worldY + solidAreaDefaultY);
        int rangePx = visionRangeTiles * gamePanel.tileSize;
        return (long) vx * vx + (long) vy * vy <= (long) rangePx * rangePx;
    }

    private void steerTowardsPlayer() {
        if (gamePanel == null || gamePanel.player == null)
            return;
        int dx = (gamePanel.player.worldX + gamePanel.player.solidAreaDefaultX) - (worldX + solidAreaDefaultX);
        int dy = (gamePanel.player.worldY + gamePanel.player.solidAreaDefaultY) - (worldY + solidAreaDefaultY);
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = dx < 0 ? "left" : "right";
        } else {
            direction = dy < 0 ? "up" : "down";
        }
    }

    public void checkDrop() {
        // roll for drop
        int i = new Random().nextInt(100) + 1;

        // set the monster drop
        if (i < 50) {
            dropItem(new OBJ_Coin(gp));
        }
        if (i >= 50 && i < 75) {
            dropItem(new OBJ_Heart(gp));
        }

        if (i >= 75 && i < 100) {
            dropItem(new OBJ_ManaCrystal(gp));
        }
    }
}
