package src.entity;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import src.main.GamePanel;
import src.main.UtilityTool;

public class Entity {
    protected GamePanel gp; // Reference to the GamePanel for accessing game state
    public int worldX, worldY; // World coordinates of the entity
    public int screenX, screenY; // Screen coordinates of the entity

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2; // Images for animations
    public String direction = "down"; // Direction of the entity
    public BufferedImage image, image2, image3; // Images for the entity


    public BufferedImage[] animationFrames; // Array for animation frames
    public boolean collision = false;
    public boolean invincible = false; // New variable for invincibility
    public int invincibleCounter = 0; // Counter for invincibility frames
    boolean attacking = false; // New variable for attack state
    public boolean alive = true;
    public boolean dying = false; // New variable for death state
    public int dyingCounter = 0; // Counter for dying frames

    public int spriteCounter = 0; // Counter for animation frames
    public int spriteNum = 1; // Current sprite number for animation
    public Rectangle solidArea = new Rectangle(18, 50, 13, 13); // Collision area for the entity
    public Rectangle attackArea = new Rectangle(0, 0, 0, 0); // Area for attack collision
    public boolean collisionOn = false; // Flag for collision detection
    public int type; // 0 = Player, 1 = NPC, 2 = Monster

    public int solidAreaDefaultX, solidAreaDefaultY; // Default position of the solid area

    // Animation system variables
    public BufferedImage[][] animationImages = new BufferedImage[5][6]; // [direction][frame]
    public int frameIndex = 0; // Current frame index for animation
    public int idleFrame = 1; // Current idle frame

    public int actionLockCounter = 0; // Counter to lock actions for a certain period
    String dialogues[] = new String[20]; // Array to hold dialogue strings
    int dialogueIndex = 0; // Current dialogue index

    // Character attributes
    public String name;
    public int maxLife;
    public int life;
    public int speed; // Speed of the entity
    public int level;
    public int strength;
    public int dexterity;
    public int attack;
    public int defense;
    public int exp;
    public int nextLevelExp;
    public int coin;
    public Entity currentWeapon;
    public Entity currentShield;

    // TYPE
    public final int TYPE_PLAYER = 0;
    public final int TYPE_NPC = 1;
    public final int TYPE_MONSTER = 2;
    public final int TYPE_SWORD = 3;
    public final int TYPE_AXE = 4;
    public final int TYPE_SHIELD = 5;
    public final int TYPE_CONSUMABLE = 6;


    // ITEM Attributes
    public int attackValue;
    public int defenseValue;
    public String description = "";

    public void setAction() {}
    public void damageReaction() {}

    public void speak() {
        // NPC should face OPPOSITE direction of player (face towards player)
        switch (gp.player.direction) {
            case "up":
                direction = "down"; // Player facing up, NPC faces down (towards player)
                break;
            case "down":
                direction = "up"; // Player facing down, NPC faces up (towards player)
                break;
            case "left":
                direction = "right"; // Player facing left, NPC faces right (towards player)
                break;
            case "right":
                direction = "left"; // Player facing right, NPC faces left (towards player)
                break;
        }
    }

    public void use(Entity entity) {}

    public void update() {
        setAction();

        collisionOn = false; // Reset collision flag before moving
        gp.cChecker.checkTile(this); // Check for tile collisions
        gp.cChecker.checkObject(this, false); // Check for object collisions
        gp.cChecker.checkEntity(this, gp.monster); // Check for monster collisions
        boolean contactPlayer = gp.cChecker.checkPlayer(this);

        // Monster damages player on contact with invincibility cooldown
        if (this.type == TYPE_MONSTER && contactPlayer && this.alive && !this.dying) {
            if (!gp.player.invincible) {
                // Play received-damage SFX (index 7), not swing
                gp.playSoundEffect(7);
                int damage = attack - gp.player.defense;
                if (damage < 0)
                    damage = 0;
                gp.player.life -= damage;
                gp.player.invincible = true; // start i-frames
                gp.player.invincibleCounter = 0;
            }
        }

        // Check for object collisions only if no tile collision
        if (!collisionOn) {
            gp.cChecker.checkObject(this, false); // Check for object collisions
        }

        // Check for entity collisions (NPC vs NPC, NPC vs Player)
        if (!collisionOn) {
            gp.cChecker.checkEntity(this, gp.npc); // Check for NPC collisions
        }
        
        // Check for player collision (prevent NPC from walking through player)
        if (!collisionOn) {
            boolean playerCollision = gp.cChecker.checkPlayer(this); // Check if NPC hits player
            if (playerCollision) {
                collisionOn = true; // Block movement if hitting player
            }
        }

        // Only move if no collision detected
        if (!collisionOn) {
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
                case "idle":
                    // Idle logic can be added here
                    break;
            }
        }

        spriteCounter++;
        if (spriteCounter > 10) { // Change frame every 10 updates
            spriteCounter = 0;
            frameIndex++;
            if (frameIndex >= animationImages[0].length) {
                frameIndex = 0; // Reset to first frame
            }
        }

        // Handle player's invincibility countdown (i-frames)
        if (gp.player != null && gp.player.invincible) {
            gp.player.invincibleCounter++;
            if (gp.player.invincibleCounter > 60) { // ~1 second at 60 FPS
                gp.player.invincible = false;
                gp.player.invincibleCounter = 0;
            }
        }

        // Handle this entity's own invincibility (e.g., monsters)
        if (this != gp.player && this.invincible) {
            this.invincibleCounter++;
            if (this.invincibleCounter > 30) { // half a second of i-frames
                this.invincible = false;
                this.invincibleCounter = 0;
            }
        }
        // Note: Dying fade is applied during draw(), not here (no Graphics2D in update()).
    }

    public void dyingAnimation(Graphics2D g2) {
        dyingCounter++;

        int i = 5;
        if (dyingCounter <= i) {changeAlpha(g2, 0f);}
        if (dyingCounter > i && dyingCounter <= i * 2) {changeAlpha(g2, 0.1f);}
        if (dyingCounter > i * 2 && dyingCounter <= i * 3) {changeAlpha(g2, 0.2f);}
        if (dyingCounter > i * 3 && dyingCounter <= i * 4) {changeAlpha(g2, 0.3f);}
        if (dyingCounter > i * 4 && dyingCounter <= i * 5) {changeAlpha(g2, 0.4f);}
        if (dyingCounter > i * 5 && dyingCounter <= i * 6) {changeAlpha(g2, 0.5f);}
        if (dyingCounter > i * 6 && dyingCounter <= i * 7) {changeAlpha(g2, 0.6f);}
        if (dyingCounter > i * 7 && dyingCounter <= i * 8) {changeAlpha(g2, 0.7f);}

        if (dyingCounter > i * 8) { // After 30 frames, remove the entity
            dying = false;
            alive = false;
        }
    }

    public void changeAlpha(Graphics2D g2, float alphaValue) {
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue);
        g2.setComposite(ac);
    }


    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        // Set the position on the screen based on world coordinates
        screenX = worldX - gp.player.worldX + gp.player.screenX;
        screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Only draw if the entity is visible on screen
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            BufferedImage image = null;
            int directionIndex;

            // Select appropriate image based on direction
            switch (direction) {
                case "up":
                    directionIndex = 0;
                    if (animationImages[directionIndex][frameIndex] != null) {
                        image = animationImages[directionIndex][frameIndex];
                    } else {
                        image = up1; // Fallback to old system
                    }
                    break;
                case "down":
                    directionIndex = 1;
                    if (animationImages[directionIndex][frameIndex] != null) {
                        image = animationImages[directionIndex][frameIndex];
                    } else {
                        image = down1; // Fallback to old system
                    }
                    break;
                case "left":
                    directionIndex = 2;
                    if (animationImages[directionIndex][frameIndex] != null) {
                        image = animationImages[directionIndex][frameIndex];
                    } else {
                        image = left1; // Fallback to old system
                    }
                    break;
                case "right":
                    directionIndex = 3;
                    if (animationImages[directionIndex][frameIndex] != null) {
                        image = animationImages[directionIndex][frameIndex];
                    } else {
                        image = right1; // Fallback to old system
                    }
                    break;
                case "idle":
                    directionIndex = 4;
                    int idleFrameIndex = Math.min(idleFrame - 1, 5);
                    if (animationImages[directionIndex][idleFrameIndex] != null) {
                        image = animationImages[directionIndex][idleFrameIndex];
                    } else {
                        image = down1; // Fallback to old system
                    }
                    break;
                default:
                    // Default fallback
                    if (animationImages[1][0] != null) {
                        image = animationImages[1][0]; // Down direction, first frame
                    } else {
                        image = down1; // Fallback to old system
                    }
                    break;
            }

            // Draw the entity image with dying/invincibility feedback
            if (image != null) {
                Composite old = g2.getComposite();
                // Apply dying fade first (takes precedence over invincibility)
                if (dying) {
                    dyingAnimation(g2); // sets alpha based on dyingCounter progression
                } else if (this != gp.player && this.invincible) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                }
                g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                g2.setComposite(old);
            }
        }
    }

    public void draw(Graphics2D g2) {
        draw(g2, this.gp);
    }

    public BufferedImage setup(String imagePath) {

        UtilityTool uTool = new UtilityTool();
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath));
            // Scale the image to the tile size
            image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public void getImage() {
        throw new UnsupportedOperationException("Unimplemented method 'getImage'");
    }
}
