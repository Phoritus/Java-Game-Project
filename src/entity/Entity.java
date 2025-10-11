package src.entity;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import src.main.GamePanel;
import src.main.UtilityTool;

public class Entity implements src.interfaces.Updatable, src.interfaces.Drawable {

    // State
    protected GamePanel gp;
    public int worldX, worldY; // World coordinates of the entity
    public int screenX, screenY; // Screen coordinates of the entity
    public boolean sleep = false;
    public boolean temp = false;
    public boolean drawing = true;

    public BufferedImage up1, up2, up3, up4, up5; // Images for animations
    public BufferedImage down1, down2, down3, down4, down5;
    public BufferedImage left1, left2, left3, left4, left5, left6, left7;
    public BufferedImage right1, right2, right3, right4, right5;
    public String direction = "down"; // Direction of the entity
    public BufferedImage image, image2, image3; // Images for the entity
    public int shotAvailableCounter = 0;

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
    public Rectangle solidArea = new Rectangle(18, 50, 13, 13); // Default; projectiles will override after init
    public Rectangle attackArea = new Rectangle(0, 0, 0, 0); // Area for attack collision
    public boolean collisionOn = false; // Flag for collision detection
    public int type; // 0 = Player, 1 = NPC, 2 = Monster

    public int solidAreaDefaultX, solidAreaDefaultY; // Default position of the solid area

    // Animation system variables
    public BufferedImage[][] animationImages = new BufferedImage[5][16]; // [direction][frame] - increased to 16 frames
    public int frameIndex = 0; // Current frame index for animation
    public int idleFrame = 1; // Current idle frame
    public int motion1_duration = 0; // Duration for motion/attack pattern 1
    public int motion2_duration = 0; // Duration for motion/attack pattern 2

    public int actionLockCounter = 0; // Counter to lock actions for a certain period
    public String dialogues[][] = new String[20][20]; // 2D Array to hold dialogue strings [set][line]
    public int dialogueSet = 0; // Current dialogue set (for different conversation branches)
    public int dialogueIndex = 0; // Current dialogue index within the set

    // Character attributes
    public String name;
    public int maxLife;
    public int life;
    public int speed; // Speed of the entity
    public int maxMana;
    public int mana;
    public int ammo;
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
    public Entity currentLight;
    public Projectile projectile;
    public int value;

    // TYPE
    public final int TYPE_PLAYER = 0;
    public final int TYPE_NPC = 1;
    public final int TYPE_MONSTER = 2;
    public final int TYPE_SWORD = 3;
    public final int TYPE_AXE = 4;
    public final int TYPE_SHIELD = 5;
    public final int TYPE_CONSUMABLE = 6;
    public final int TYPE_PICKUP_ONLY = 7;
    public final int TYPE_OBSTACLE = 8;
    public final int TYPE_HOUSE = 9;
    public final int TYPE_LIGHT = 10;

    // ITEM Attributes
    public int attackValue;
    public int defenseValue;
    public String description = "";
    public int useCost;
    public ArrayList<Entity> inventory = new ArrayList<>();
    public final int inventorySize = 30; // Maximum inventory slots (6x5 grid)
    public int price;
    public int lightRadius; // For light sources

    public void setAction() {
    }

    public void damageReaction() {
    }

    public void checkDrop() {
    }

    public void dropItem(Entity droppedItem) {
        for (int i = 0; i < gp.obj[0].length; i++) {
            if (gp.obj[gp.currentMap][i] == null) {
                gp.obj[gp.currentMap][i] = droppedItem;
                gp.obj[gp.currentMap][i].worldX = this.worldX; // the dead
                gp.obj[gp.currentMap][i].worldY = this.worldY;
                break;
            }
        }
    }

    public void speak() {}

    public void facePlayer() {
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

    public void startDialogue(Entity entity, int setNum) {
        
        gp.gameState = gp.dialogueState;
        gp.ui.npc = entity;
        dialogueSet = setNum;
    }

    public boolean use(Entity entity) {
        return false;
    }

    public void update() {
        if (!sleep) {
            setAction();

            collisionOn = false; // Reset collision flag before moving
            gp.cChecker.checkTile(this); // Check for tile collisions
            gp.cChecker.checkObject(this, false); // Check for object collisions
            gp.cChecker.checkEntity(this, gp.monster); // Check for monster collisions
            boolean contactPlayer = gp.cChecker.checkPlayer(this);

            // Monster damages player on contact with invincibility cooldown
            if (this.type == TYPE_MONSTER && contactPlayer && this.alive && !this.dying) {
                damagePlayer(attack);
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

            // Player's invincibility countdown is handled in Player.update() to avoid
            // incrementing multiple times per frame (once per entity update).

            // Handle this entity's own invincibility (e.g., monsters)
            if (this != gp.player && this.invincible) {
                this.invincibleCounter++;
                if (this.invincibleCounter > 30) { // half a second of i-frames
                    this.invincible = false;
                    this.invincibleCounter = 0;
                }
            }
            if (shotAvailableCounter < 30) {
                shotAvailableCounter++;
            }
        }
    }

    public void dyingAnimation(Graphics2D g2) {
        dyingCounter++;

        int i = 5;
        if (dyingCounter <= i) {
            changeAlpha(g2, 0f);
        }
        if (dyingCounter > i && dyingCounter <= i * 2) {
            changeAlpha(g2, 0.1f);
        }
        if (dyingCounter > i * 2 && dyingCounter <= i * 3) {
            changeAlpha(g2, 0.2f);
        }
        if (dyingCounter > i * 3 && dyingCounter <= i * 4) {
            changeAlpha(g2, 0.3f);
        }
        if (dyingCounter > i * 4 && dyingCounter <= i * 5) {
            changeAlpha(g2, 0.4f);
        }
        if (dyingCounter > i * 5 && dyingCounter <= i * 6) {
            changeAlpha(g2, 0.5f);
        }
        if (dyingCounter > i * 6 && dyingCounter <= i * 7) {
            changeAlpha(g2, 0.6f);
        }
        if (dyingCounter > i * 7 && dyingCounter <= i * 8) {
            changeAlpha(g2, 0.7f);
        }

        if (dyingCounter > i * 8) { // After 30 frames, remove the entity
            alive = false;
        }
    }

    public void changeAlpha(Graphics2D g2, float alphaValue) {
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue);
        g2.setComposite(ac);
    }

    public Color getParticleColor() {
        return null;
    }

    public int getParticleSize() {
        return 0;
    }

    public int getParticleSpeed() {
        return 0;
    }

    public int getParticleMaxLife() {
        return 0;
    }

    public void generateParticles(Entity generator, Entity target) {
        Color color = generator.getParticleColor();
        int size = generator.getParticleSize();
        int speed = generator.getParticleSpeed();
        int maxLife = generator.getParticleMaxLife();

        Particle p1 = new Particle(gp, color, target, size, maxLife, speed, -2, -1);
        Particle p2 = new Particle(gp, color, target, size, maxLife, speed, 2, -1);
        Particle p3 = new Particle(gp, color, target, size, maxLife, speed, -2, 1);
        Particle p4 = new Particle(gp, color, target, size, maxLife, speed, 2, 1);
        gp.particleList.add(p1);
        gp.particleList.add(p2);
        gp.particleList.add(p3);
        gp.particleList.add(p4);
    }

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public void damagePlayer(int attack) {
        // Only damage if the player is not currently invincible
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
            // Try absolute path lookup first
            java.io.InputStream is = getClass().getResourceAsStream(imagePath);
            if (is == null) {
                // Fallback: try relative path (without leading slash)
                String rel = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                is = getClass().getResourceAsStream(rel);
            }
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + imagePath);
            }
            image = ImageIO.read(is);
            // Scale the image to the tile size
            image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    // Overloaded setup method with custom width and height
    public BufferedImage setup(String imagePath, int width, int height) {
        UtilityTool uTool = new UtilityTool();
        BufferedImage image = null;
        try {
            // Try absolute path lookup first
            java.io.InputStream is = getClass().getResourceAsStream(imagePath);
            if (is == null) {
                // Fallback: try relative path (without leading slash)
                String rel = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                is = getClass().getResourceAsStream(rel);
            }
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + imagePath);
            }
            image = ImageIO.read(is);
            // Scale the image to the specified width and height
            image = uTool.scaleImage(image, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public void getImage() {
        throw new UnsupportedOperationException("Unimplemented method 'getImage'");
    }
}
