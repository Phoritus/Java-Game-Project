package src.entity;

import src.main.KeyHandler;
import src.main.GamePanel;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Image;


public class Player extends Entity {
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;

    // Animation images for all directions (6 frames each)
    public BufferedImage[][] animationImages = new BufferedImage[5][6]; // [direction][frame]
    // 0=up, 1=down, 2=left, 3=right, 4=idle

    public int idleCounter = 0;
    public int idleFrame = 1;

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp); 
        
        this.keyH = keyH;

        this.screenX = gp.screenWidth / 2 - (gp.tileSize / 2); // Center player on screen
        this.screenY = gp.screenHeight / 2 - (gp.tileSize / 2); // Center player on screen

        solidArea = new Rectangle(18, 42, 13, 18); // Larger collision area for better movement
        solidAreaDefaultX = solidArea.x; // Default X position of the solid area
        solidAreaDefaultY = solidArea.y; // Default Y position of the solid area
        setDefaultValues(); // Set initial position and speed
        getPlayerImage(); // Load player images
    }

    public void setDefaultValues() {
        worldX = gp.tileSize * 23; // Initial X position - center of 50x50 map
        worldY = gp.tileSize * 22; // Initial Y position - center of 50x50 map
        speed = 4; // Speed of player movement - fixed at 4
        direction = "down"; // Default direction

        // Player status
        maxLife = 6; // Maximum life points
        life = maxLife; // Start with full life
    }

    public void interctNPC(int index) {
        if (index != -1) { // If an NPC is collided
            if (gp.keyHandler.enterPressed) {
                gp.gameState = gp.dialogState; // Change game state to dialog
                gp.npc[index].speak(); // Call the speak method of the NPC
            }
            
        }
        gp.keyHandler.enterPressed = false; // Reset enter key after interaction
    }

    public void getPlayerImage() {
        try {
            // Load images into 2D array [direction][frame]
            String[][] imagePaths = {
                // Up direction (0)
                {"res/player/player_up/up1.png", "res/player/player_up/up2.png", 
                 "res/player/player_up/up3.png", "res/player/player_up/up4.png", 
                 "res/player/player_up/up5.png", "res/player/player_up/up6.png"},
                // Down direction (1)
                {"res/player/player_down/d1.png", "res/player/player_down/d2.png", 
                 "res/player/player_down/d3.png", "res/player/player_down/d4.png", 
                 "res/player/player_down/d5.png", "res/player/player_down/d6.png"},
                // Left direction (2)
                {"res/player/player_left/left1.png", "res/player/player_left/left2.png", 
                 "res/player/player_left/left3.png", "res/player/player_left/left4.png", 
                 "res/player/player_left/left5.png", "res/player/player_left/left6.png"},
                // Right direction (3)
                {"res/player/player_right/right1.png", "res/player/player_right/right2.png", 
                 "res/player/player_right/right3.png", "res/player/player_right/right4.png", 
                 "res/player/player_right/right5.png", "res/player/player_right/right6.png"},
                // Idle direction (4)
                {"res/player/player_idle/idle1.png", "res/player/player_idle/idle2.png", 
                 "res/player/player_idle/idle3.png", "res/player/player_idle/idle4.png", 
                 "res/player/player_idle/idle5.png", "res/player/player_idle/idle6.png"}
            };
            
            // Load all images
            for (int direction = 0; direction < imagePaths.length; direction++) {
                for (int frame = 0; frame < imagePaths[direction].length; frame++) {
                    animationImages[direction][frame] = setup(imagePaths[direction][frame]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();    
        }
    }

    public BufferedImage setup(String imagePath) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new java.io.File(imagePath));
            // Scale the image to the tile size
            Image scaledImage = image.getScaledInstance(gp.tileSize, gp.tileSize, Image.SCALE_SMOOTH);
            image = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
            image.getGraphics().drawImage(scaledImage, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public void update() {
        // Force speed to be 4 always (prevent speed bugs)
        speed = 4;
        
        // Don't update player movement during dialogue
        if (gp.gameState == gp.dialogState) {
            // Keep idle animation during dialogue
            handleIdleState();
            return;
        }
        
        boolean isMoving = keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;
        
        if (isMoving) {
            // Handle movement and direction
            String newDirection = direction;
            if (keyH.upPressed) newDirection = "up";
            else if (keyH.downPressed) newDirection = "down";
            else if (keyH.leftPressed) newDirection = "left";
            else if (keyH.rightPressed) newDirection = "right";
            
            direction = newDirection;

            // Check collision first before moving
            collisionOn = false;
            gp.cChecker.checkTile(this);

            // Check for object collisions and handle them
            int objIndex = gp.cChecker.checkObject(this, true);
            pickUpObject(objIndex);

            //Check Event
            gp.eventHandler.checkEvent();

            // Check NPC collision
            int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
            interctNPC(npcIndex);

            

            // Only move if there's no collision
            if (!collisionOn) {
                

                updatePosition();
            }

            
            updateMovementAnimation();
            resetIdleAnimation();
        } else {
            handleIdleState();
        }
        
    }
    
    private void updatePosition() {
        // Simple fixed movement - no speed multiplication or accumulation
        switch (direction) {
            case "up": worldY -= 4; break;      // Fixed speed 4
            case "down": worldY += 4; break;    // Fixed speed 4
            case "left": worldX -= 4; break;    // Fixed speed 4
            case "right": worldX += 4; break;   // Fixed speed 4
        }
    }
    
    private void updateMovementAnimation() {
        spriteCounter++;
        if (spriteCounter > 6) {
            spriteCounter = 0;
            spriteNum = (spriteNum >= 6) ? 1 : spriteNum + 1;
        }
    }
    
    private void resetIdleAnimation() {
        idleCounter = 0;
        idleFrame = 1;
    }
    
    private void handleIdleState() {
        direction = "idle";
        spriteNum = 1;
        spriteCounter = 0;
        
        idleCounter++;
        if (idleCounter > 8) {
            idleCounter = 0;
            idleFrame = (idleFrame >= 6) ? 1 : idleFrame + 1;
        }
    }

    public void pickUpObject(int index) {
        // Handle picking up an object
        if (index != -1) { // -1 means no object collision
            
        }
    }

    public void draw(Graphics2D g2) {
        draw(g2, gp); // Call the parent method with GamePanel parameter
    }
    
    public void draw(Graphics2D g2, GamePanel gp) {
        BufferedImage image = null;

        // Get current frame index based on direction and animation state
        int frameIndex = Math.min(spriteNum - 1, 5); // Ensure frame index is within bounds
        int directionIndex;
        
        switch (direction) {
            case "up":
                directionIndex = 0;
                image = animationImages[directionIndex][frameIndex];
                break;
            case "down":
                directionIndex = 1;
                image = animationImages[directionIndex][frameIndex];
                break;
            case "left":
                directionIndex = 2;
                image = animationImages[directionIndex][frameIndex];
                break;
            case "right":
                directionIndex = 3;
                image = animationImages[directionIndex][frameIndex];
                break;
            case "idle":
                directionIndex = 4;
                int idleFrameIndex = Math.min(idleFrame - 1, 5);
                image = animationImages[directionIndex][idleFrameIndex];
                break;
            default:
                // Default fallback
                image = animationImages[1][0]; // Down direction, first frame
                break;
        }

        if (image != null) {
            // Zoom the player model to be 2 times larger
            int scaledSize = (int)(gp.tileSize * 2.5);
            // Draw player at center of screen
            int centerX = gp.screenWidth / 2 - scaledSize / 2;
            int centerY = gp.screenHeight / 2 - scaledSize / 2;
            g2.drawImage(image, centerX, centerY, scaledSize, scaledSize, null);
        } else {
            // Fallback: White rectangle if no image is available
            g2.setColor(Color.WHITE);
            int scaledSize = (int)(gp.tileSize * 2);
            // Draw fallback at center of screen
            int centerX = gp.screenWidth / 2 - scaledSize / 2;
            int centerY = gp.screenHeight / 2 - scaledSize / 2;
            g2.fillRect(centerX, centerY, scaledSize, scaledSize);
        }

        // Draw solid area for debugging (hit box)
        // g2.setColor(Color.RED);
        // int solidAreaScreenX = screenX + solidArea.x;
        // int solidAreaScreenY = screenY + solidArea.y;
        // g2.drawRect(solidAreaScreenX, solidAreaScreenY, solidArea.width, solidArea.height);
    }
}
