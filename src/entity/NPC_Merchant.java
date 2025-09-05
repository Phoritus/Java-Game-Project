package src.entity;

import src.main.GamePanel;
import src.object.OBJ_Axe;
import src.object.OBJ_Demon_shield;
import src.object.OBJ_Key;
import src.object.OBJ_Normal_Sword;
import src.object.OBJ_Potion;
import src.object.OBJ_Premium_Shield;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Rectangle;

public class NPC_Merchant extends Entity {

    // Animation variables
    public int animationCounter = 0;
    public int animationSpeed = 10; // Change frame every 10 game updates
    public int currentFrame = 0;
    // Faster idle animation just for this NPC
    private int idleAnimCounter = 0;
    private int idleAnimSpeedTicks = 6; // lower = faster

    // Dialogue cooldown to prevent immediate movement after talking
    public int dialogueCooldown = 0;

    public NPC_Merchant(GamePanel gp) {
        super(gp); // Call the parent constructor

        direction = "idle"; // Default direction for the NPC (idle animation)
        type = TYPE_NPC; // Ensure this entity is recognized as an NPC
        collision = true; // NPC should be solid (block player and other entities)

        getNPCImage(); // Load the merchant images
        setDialogue(); // Set initial dialogue for the NPC
        setItems(); // Set items the merchant sells

        // Set collision area same as player for consistency
        solidArea = new Rectangle(33, 32, 25, 30);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
    }

    public void getNPCImage() {
        try {
            // Load merchant images into 2D array [direction][frame]
            String[] frames = {
                    "res/npc/merchant/g0.png", "res/npc/merchant/g1.png",
                    "res/npc/merchant/g2.png", "res/npc/merchant/g3.png",
                    "res/npc/merchant/g4.png", "res/npc/merchant/g5.png"
            };
            // Populate base class animationImages (5 x 6). We'll use index 1 (down) and 4
            // (idle)
            for (int f = 0; f < frames.length; f++) {
                this.animationImages[1][f] = setup(frames[f]); // down direction frames
                this.animationImages[4][f] = this.animationImages[1][f]; // reuse for idle
            }
            // Set a fallback frame for older draw paths
            this.down1 = this.animationImages[1][0];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedImage setup(String imagePath) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new java.io.File(imagePath));
            int scaledSize = gp.tileSize;
            Image scaledImage = image.getScaledInstance(scaledSize, scaledSize, Image.SCALE_SMOOTH);
            image = new BufferedImage(scaledSize, scaledSize, BufferedImage.TYPE_INT_ARGB);
            image.getGraphics().drawImage(scaledImage, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void update() {
        // Merchant stands still and idles; ensure direction is idle
        direction = "idle";

        // Let base class handle generic timers/invincibility, etc.
        super.update();

        // Advance idle animation faster
        idleAnimCounter++;
        if (idleAnimCounter >= idleAnimSpeedTicks) {
            idleAnimCounter = 0;
            idleFrame++;
            if (idleFrame > 6)
                idleFrame = 1;
        }
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Calculate on-screen position relative to player
        screenX = worldX - gp.player.worldX + gp.player.screenX;
        screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Only draw if the entity is visible on screen
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            // Pick current idle frame
            BufferedImage image = null;
            int idleIndex = Math.min(idleFrame - 1, 5);
            if (animationImages[4][idleIndex] != null) {
                image = animationImages[4][idleIndex];
            } else if (animationImages[1][0] != null) {
                image = animationImages[1][0];
            } else {
                image = down1;
            }

            // Draw slightly bigger than a tile and keep feet anchored to the ground
            float scale = 2.3f; // enlarge NPC
            int w = Math.round(gp.tileSize * scale);
            int h = Math.round(gp.tileSize * scale);
            int dx = screenX + (gp.tileSize - w) / 2; // center horizontally
            int dy = screenY + (gp.tileSize - h); // anchor feet

            g2.drawImage(image, dx, dy, w, h, null);
        }
    }

    public void setDialogue() {
        dialogues[0] = "He he, so you found me.\n Looking for something?";

    }

    public void setItems() {
        inventory.add(new OBJ_Axe(gp));
        inventory.add(new OBJ_Key(gp));
        inventory.add(new OBJ_Demon_shield(gp));
        inventory.add(new OBJ_Potion(gp));
        inventory.add(new OBJ_Normal_Sword(gp));
        inventory.add(new OBJ_Premium_Shield(gp));
    }

    public void speak() {
        super.speak();
        // Provide dialogue text for the trade overlay's dialog window
        if (dialogues != null) {
            String line = dialogues[Math.max(0, Math.min(9, dialogueIndex))];
            if (line == null || line.isEmpty()) {
                line = dialogues[0];
            }
            if (line != null) {
                gp.ui.currentDialogue = line;
            }
        }
        gp.gameState = gp.tradeState;
        gp.ui.npc = this;
    }
}
