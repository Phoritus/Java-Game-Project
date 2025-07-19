package src.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import src.main.GamePanel;
import src.main.UtilityTool;

public class Entity {
    GamePanel gp; // Reference to the GamePanel for accessing game state
    public int worldX, worldY; // World coordinates of the entity
    public int screenX, screenY; // Screen coordinates of the entity
    public int speed; // Speed of the entity

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2; // Images for animations
    public String direction = "down"; // Direction of the entity
    public BufferedImage image, image2, image3; // Images for the entity
    
    public BufferedImage[] animationFrames; // Array for animation frames
    public String name;
    public boolean collision = false;

    public int spriteCounter = 0; // Counter for animation frames
    public int spriteNum = 1; // Current sprite number for animation
    public Rectangle solidArea = new Rectangle(18, 50, 13, 13); // Collision area for the entity
    public boolean collisionOn = false; // Flag for collision detection

    public int solidAreaDefaultX, solidAreaDefaultY; // Default position of the solid area

    // Animation system variables
    public BufferedImage[][] animationImages = new BufferedImage[5][6]; // [direction][frame]
    public int frameIndex = 0; // Current frame index for animation
    public int idleFrame = 1; // Current idle frame

    public int actionLockCounter = 0; // Counter to lock actions for a certain period
    String dialogues[] = new String[20]; // Array to hold dialogue strings
    int dialogueIndex = 0; // Current dialogue index

    // Character status
    public int maxLife;
    public int life;

    public void setAction() {}

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

    public void update() {
        setAction();

        collisionOn = false; // Reset collision flag before moving
        gp.cChecker.checkTile(this); // Check for tile collisions

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
            int playerIndex = gp.cChecker.checkPlayer(this); // Check if NPC hits player
            if (playerIndex != -1) {
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

            // Draw the entity image
            if (image != null) {
                g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
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
}
