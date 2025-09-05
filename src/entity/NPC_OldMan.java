package src.entity;

import src.main.GamePanel;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Rectangle;

public class NPC_OldMan extends Entity {

    // Animation images for all directions
    public BufferedImage[][] animationImages = new BufferedImage[5][]; // [direction][frame]
    // 0=up, 1=down, 2=left, 3=right, 4=idle

    // Animation variables
    public int animationCounter = 0;
    public int animationSpeed = 10; // Change frame every 10 game updates
    public int currentFrame = 0;

    // Dialogue cooldown to prevent immediate movement after talking
    public int dialogueCooldown = 0;

    public NPC_OldMan(GamePanel gp) {
        super(gp); // Call the parent constructor

        direction = "idle"; // Default direction for the NPC (idle animation)
        speed = 1; // NPCs typically do not move, so speed is set to 1
        type = TYPE_NPC; // Ensure this entity is recognized as an NPC
        collision = true; // NPC should be solid (block player and other entities)

        getNPCImage(); // Load the priest images
        setDialogue(); // Set initial dialogue for the NPC

        // Set collision area same as player for consistency
        solidArea = new Rectangle(33, 32, 25, 30);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    public void getNPCImage() {
        try {
            // Load priest images into 2D array [direction][frame]
            String[][] imagePaths = {
                    // Up direction (0) - 6 frames
                    { "res/npc/priest/go_up/up1.png", "res/npc/priest/go_up/up2.png",
                            "res/npc/priest/go_up/up3.png", "res/npc/priest/go_up/up4.png",
                            "res/npc/priest/go_up/up5.png", "res/npc/priest/go_up/up6.png" },
                    // Down direction (1) - 5 frames
                    { "res/npc/priest/go_down/down1.png", "res/npc/priest/go_down/down2.png",
                            "res/npc/priest/go_down/down3.png", "res/npc/priest/go_down/down4.png",
                            "res/npc/priest/go_down/down5.png" },
                    // Left direction (2) - 6 frames
                    { "res/npc/priest/left/left1.png", "res/npc/priest/left/left2.png",
                            "res/npc/priest/left/left3.png", "res/npc/priest/left/left4.png",
                            "res/npc/priest/left/left5.png", "res/npc/priest/left/left6.png" },
                    // Right direction (3) - 6 frames
                    { "res/npc/priest/right/right1.png", "res/npc/priest/right/right2.png",
                            "res/npc/priest/right/right3.png", "res/npc/priest/right/right4.png",
                            "res/npc/priest/right/right5.png", "res/npc/priest/right/right6.png" },
                    // Idle direction (4) - 4 frames
                    { "res/npc/priest/idle/idle1.png", "res/npc/priest/idle/idle2.png",
                            "res/npc/priest/idle/idle3.png", "res/npc/priest/idle/idle4.png" }
            };

            // Initialize arrays with correct sizes
            for (int direction = 0; direction < imagePaths.length; direction++) {
                animationImages[direction] = new BufferedImage[imagePaths[direction].length];
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
            // Scale the image to 2.5 times the tile size (same as player)
            int scaledSize = (int) (gp.tileSize * 1.85);
            Image scaledImage = image.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH);
            image = new BufferedImage(scaledSize, scaledSize, BufferedImage.TYPE_INT_ARGB);
            image.getGraphics().drawImage(scaledImage, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public void updateAnimation() {
        animationCounter++;
        if (animationCounter > animationSpeed) {
            // Get current direction's frame count
            int directionIndex = getDirectionIndex();
            int maxFrames = animationImages[directionIndex].length;

            currentFrame++;
            if (currentFrame >= maxFrames) {
                currentFrame = 0;
            }
            animationCounter = 0;
        }
    }

    public int getDirectionIndex() {
        switch (direction) {
            case "up":
                return 0;
            case "down":
                return 1;
            case "left":
                return 2;
            case "right":
                return 3;
            case "idle":
            default:
                return 4; // Idle direction
        }
    }

    public void setDialogue() {
        dialogues[0] = "Hello, traveler! Welcome \nto our Forest.";
        dialogues[1] = "Beware of the dangers that \nlurk within.";
        dialogues[2] = "If you seek wisdom, you may \nfind it here.";
        dialogues[3] = "The forest holds many secrets.";
        dialogues[4] = "Remember to always \nbe cautious.";
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Update animation
        updateAnimation();

        // Set the position on the screen based on world coordinates
        screenX = worldX - gp.player.worldX + gp.player.screenX;
        screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Only draw if the entity is visible on screen
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            BufferedImage image = null;
            int directionIndex = getDirectionIndex();

            // Get current animation frame
            if (animationImages[directionIndex] != null &&
                    currentFrame < animationImages[directionIndex].length) {
                image = animationImages[directionIndex][currentFrame];
            }

            // Draw the NPC image
            if (image != null) {
                // Image is already scaled to 2.5 times tile size in setup method
                g2.drawImage(image, screenX, screenY, null);
            }

            // Display hit/collision box for debugging
            // g2.setColor(Color.RED);
            // g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width,
            // solidArea.height);
        }
    }

    public void setAction() {
        // Decrease dialogue cooldown
        if (dialogueCooldown > 0) {
            dialogueCooldown--;
            direction = "idle"; // Stay idle during cooldown
            return;
        }

        // Only move when not in dialogue mode
        if (gp.gameState != gp.dialogState) {
            actionLockCounter++;
            if (actionLockCounter == 120) {
                Random random = new Random();
                int action = random.nextInt(100) + 1; // Random action chance

                if (action <= 25) {
                    direction = "up";
                } else if (action > 25 && action <= 50) {
                    direction = "down";
                } else if (action > 50 && action <= 75) {
                    direction = "left";
                } else if (action > 75 && action <= 100) {
                    direction = "right";
                }

                actionLockCounter = 0; // Reset action lock counter
            }
        } else {
            // Reset to idle when in dialogue
            direction = "idle";
            actionLockCounter = 0;
        }
    }

    public void speak() {
        // Display the current dialogue line
        if (dialogues[dialogueIndex] != null) {
            gp.ui.currentDialogue = dialogues[dialogueIndex];
            dialogueIndex++;

            // Reset dialogue index if we've reached the end
            if (dialogueIndex >= dialogues.length || dialogues[dialogueIndex] == null) {
                dialogueIndex = 0;
            }
        }

        // Set dialogue cooldown to prevent immediate movement
        dialogueCooldown = 180; // 3 seconds at 60 FPS

        super.speak(); // Call the parent speak method to face the player
    }
}