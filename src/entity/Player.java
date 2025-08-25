package src.entity;

import src.main.KeyHandler;
import src.main.GamePanel;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.RenderingHints;
import src.main.UtilityTool;
import src.object.OBJ_Normal_Sword;
import src.object.OBJ_Premium_Shield;

public class Player extends Entity {
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;

    // Animation images for all directions (6 frames each)
    public BufferedImage[][] animationImages = new BufferedImage[5][6]; // [direction][frame]
    // 0=up, 1=down, 2=left, 3=right, 4=idle

    public int idleCounter = 0;
    public int idleFrame = 1;
    private int lastFacingDirIndex = 1; // 0=up,1=down,2=left,3=right

    // Attack animation state
    public BufferedImage[][] attackImages = new BufferedImage[4][4]; // [up,down,left,right][frame]
    public boolean attacking = false;
    public int attackFrameIndex = 0;
    public int attackAnimCounter = 0;
    public int attackAnimDelay = 6; // frames per attack frame
    private boolean attackHitChecked = false; // ensure single hit per attack

    // Combo tracking for hit stacks
    private int hitComboCount = 0;
    private long lastHitMillis = 0L;

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);

        this.keyH = keyH;

        this.screenX = gp.screenWidth / 2 - (gp.tileSize / 2); // Center player on screen
        this.screenY = gp.screenHeight / 2 - (gp.tileSize / 2); // Center player on screen

        solidArea = new Rectangle(18, 42, 13, 18); // Larger collision area for better movement
        solidAreaDefaultX = solidArea.x; // Default X position of the solid area
        solidAreaDefaultY = solidArea.y; // Default Y position of the solid area

        attackArea.width = 36;
        attackArea.height = 36;

        setDefaultValues(); // Set initial position and speed
        getPlayerImage(); // Load player images
        getAttackImage(); // Load attack images
    }

    public void setDefaultValues() {
        worldX = gp.tileSize * 23; // Initial X position - center of 50x50 map
        worldY = gp.tileSize * 14; // Initial Y position - center of 50x50 map
        speed = 4; // Speed of player movement - fixed at 4
        direction = "down"; // Default direction

        // Player status
        level = 1;
        maxLife = 10; // Maximum life points
        life = maxLife; // Start with full life
        strength = 1; // Affects attack power
        dexterity = 1; // More Dexterity, less damage taken
        exp = 0;
        nextLevelExp = 5;
        coin = 0;
        currentWeapon = new OBJ_Normal_Sword(gp);
        currentShield = new OBJ_Premium_Shield(gp);
        attack = getAttack(); // Calculate initial attack value
        defense = getDefense(); // Calculate initial defense value
    }

    public int getAttack() {
        return attack = strength * currentWeapon.attackValue;
    }

    public int getDefense() {
        return defense = dexterity * currentShield.defenseValue;
    }

    public void interactNPC(int index) {

        // Talk with F key only
        if (index != -1 && gp.keyHandler.fPressed) {
            gp.gameState = gp.dialogState; // Change game state to dialog
            gp.npc[index].speak(); // Call the speak method of the NPC
            gp.keyHandler.fPressed = false; // Reset F after successful interaction
        }
    }

    public void interactMonster(int index) {
        if (index != -1) { // If a monster is collided
            if (!invincible) {
                gp.playSoundEffect(7);
                int damage = gp.monster[index].attack - defense;
                if (damage < 0)
                    damage = 0;
                life -= damage;
                invincible = true; // Set invincibility after taking damage
            }
            // No need to reset enter key here; combat isn't tied to F
        }
    }

    public void damageMonster(int index) {
        if (index < 0 || index >= gp.monster.length)
            return;
        Entity m = gp.monster[index];
        if (m == null)
            return;
        if (!m.alive || m.dying)
            return; // don't hit dead/dying monsters
        if (m.type != 2)
            return; // only monsters
        if (m.invincible)
            return; // already hit recently

        gp.playSoundEffect(5);
        int damage = attack - gp.monster[index].defense;
        if (damage < 0)
            damage = 0;
    m.life -= damage;
    if (m.life < 0) m.life = 0; // clamp to avoid negative HP bar
        // Update hit combo (reset if too slow between hits)
        long now = System.currentTimeMillis();
        if (now - lastHitMillis > 3000) { // 3s window between hits
            hitComboCount = 0;
        }
        hitComboCount++;
        lastHitMillis = now;
        gp.ui.addMessage(hitComboCount + " hit");
        m.invincible = true;
        m.invincibleCounter = 0;
        // Show the monster HP bar for 10s after being hit (if the monster supports it)
        if (m instanceof src.monster.MonBlueSlime) {
            ((src.monster.MonBlueSlime) m).showHpBar();
        }
    if (m.life <= 0) {
            // Enter dying state and stop normal updates; removal happens after fade
            gp.ui.addMessage("Killed " + m.name);
            gp.ui.addMessage("Exp +" + gp.monster[index].exp);
            m.dying = true;
            m.alive = false;
            m.invincible = false;
            exp += gp.monster[index].exp; // Gain exp from monster
            checkLevelUp();
        }
    }

    public void checkLevelUp() {

        if (exp >= nextLevelExp) {

            level++;
            nextLevelExp *= 2; // Double the experience needed for the next level 
            maxLife += 2; strength++; dexterity++;
            attack = getAttack();
            defense = getDefense();

            gp.playSoundEffect(8);
            gp.gameState = gp.dialogState;
            gp.ui.currentDialogue = "You leveled up! You are now level " + level + "!\n"
                    + "Max Life increased by 2\n"
                    + "Strength increased by 1\n"
                    + "Dexterity increased by 1";
        }
    }

    public void getPlayerImage() {
        try {
            // Load images into 2D array [direction][frame]
            String[][] imagePaths = {
                    // Up direction (0)
                    { "res/player/player_up/up1.png", "res/player/player_up/up2.png",
                            "res/player/player_up/up3.png", "res/player/player_up/up4.png",
                            "res/player/player_up/up5.png", "res/player/player_up/up6.png" },
                    // Down direction (1)
                    { "res/player/player_down/d1.png", "res/player/player_down/d2.png",
                            "res/player/player_down/d3.png", "res/player/player_down/d4.png",
                            "res/player/player_down/d5.png", "res/player/player_down/d6.png" },
                    // Left direction (2)
                    { "res/player/player_left/left1.png", "res/player/player_left/left2.png",
                            "res/player/player_left/left3.png", "res/player/player_left/left4.png",
                            "res/player/player_left/left5.png", "res/player/player_left/left6.png" },
                    // Right direction (3)
                    { "res/player/player_right/right1.png", "res/player/player_right/right2.png",
                            "res/player/player_right/right3.png", "res/player/player_right/right4.png",
                            "res/player/player_right/right5.png", "res/player/player_right/right6.png" },
                    // Idle direction (4)
                    { "res/player/player_idle/idle1.png", "res/player/player_idle/idle2.png",
                            "res/player/player_idle/idle3.png", "res/player/player_idle/idle4.png",
                            "res/player/player_idle/idle5.png", "res/player/player_idle/idle6.png" }
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

    public void getAttackImage() {
        try {
            // Map: up=back, down=front, left=left, right=right
            String[][] atkPaths = new String[][] {
                    { // up (back)
                            "res/player/player_atk/b_atk/b_atk1.png",
                            "res/player/player_atk/b_atk/b_atk2.png",
                            "res/player/player_atk/b_atk/b_atk3.png",
                            "res/player/player_atk/b_atk/b_atk4.png"
                    },
                    { // down (front)
                            "res/player/player_atk/f_atk/f_atk1.png",
                            "res/player/player_atk/f_atk/f_atk2.png",
                            "res/player/player_atk/f_atk/f_atk3.png",
                            "res/player/player_atk/f_atk/f_atk4.png"
                    },
                    { // left
                            "res/player/player_atk/l_atk/l_atk1.png",
                            "res/player/player_atk/l_atk/l_atk2.png",
                            "res/player/player_atk/l_atk/l_atk3.png",
                            "res/player/player_atk/l_atk/l_atk4.png"
                    },
                    { // right
                            "res/player/player_atk/r_atk/r_atk1.png",
                            "res/player/player_atk/r_atk/r_atk2.png",
                            "res/player/player_atk/r_atk/r_atk3.png",
                            "res/player/player_atk/r_atk/r_atk4.png"
                    }
            };

            for (int d = 0; d < 4; d++) {
                for (int f = 0; f < 4; f++) {
                    attackImages[d][f] = setup(atkPaths[d][f]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startAttack() {
        attacking = true;
        attackFrameIndex = 0;
        attackAnimCounter = 0;
        attackHitChecked = false;
    }

    private void updateAttack() {
        attackAnimCounter++;
        if (attackAnimCounter > attackAnimDelay) {
            attackAnimCounter = 0;
            attackFrameIndex++;
        }

        // Trigger hit once early in the animation
        if (!attackHitChecked && attackFrameIndex >= 1) {
            checkAttackHit();
            attackHitChecked = true;
        }

        // End attack after last frame
        if (attackFrameIndex >= 4) {
            attacking = false;
            attackFrameIndex = 0;
        }
    }

    private void checkAttackHit() {
        // Player collision box in world space
        Rectangle playerBox = new Rectangle(
                worldX + solidAreaDefaultX,
                worldY + solidAreaDefaultY,
                solidArea.width,
                solidArea.height);

        // Build attack box in front of player
        int atkW = (attackArea != null && attackArea.width > 0) ? attackArea.width : gp.tileSize;
        int atkH = (attackArea != null && attackArea.height > 0) ? attackArea.height : gp.tileSize;
        Rectangle attackBox;
        switch (lastFacingDirIndex) {
            case 0: // Up
                attackBox = new Rectangle(
                        playerBox.x + (playerBox.width - atkW) / 2,
                        playerBox.y - atkH,
                        atkW, atkH);
                break;
            case 1: // Down
                attackBox = new Rectangle(
                        playerBox.x + (playerBox.width - atkW) / 2,
                        playerBox.y + playerBox.height,
                        atkW, atkH);
                break;
            case 2: // Left
                attackBox = new Rectangle(
                        playerBox.x - atkW,
                        playerBox.y + (playerBox.height - atkH) / 2,
                        atkW, atkH);
                break;
            case 3: // Right
            default:
                attackBox = new Rectangle(
                        playerBox.x + playerBox.width,
                        playerBox.y + (playerBox.height - atkH) / 2,
                        atkW, atkH);
                break;
        }

        // Check against monsters; if we hit at least one, don't play swing SFX
        boolean hitSomeone = false;
        for (int i = 0; i < gp.monster.length; i++) {
            if (gp.monster[i] == null)
                continue;
            Rectangle monBox = new Rectangle(
                    gp.monster[i].worldX + gp.monster[i].solidAreaDefaultX,
                    gp.monster[i].worldY + gp.monster[i].solidAreaDefaultY,
                    gp.monster[i].solidArea.width,
                    gp.monster[i].solidArea.height);
            if (attackBox.intersects(monBox)) {
                damageMonster(i);
                hitSomeone = true;
                // Enforce one-hit-per-attack and avoid multiple hit sounds
                break;
            }
        }
        if (!hitSomeone) {
            // Only play swing if we didn't hit anything
            gp.playSoundEffect(6);
        }
    }

    public BufferedImage setup(String imagePath) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new java.io.File(imagePath));
            // Scale the image to the tile size using nearest-neighbor for crisp pixels
            UtilityTool uTool = new UtilityTool();
            image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
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

        if (attacking) {
            // During attack, update attack frames and skip movement
            updateAttack();
        } else if (isMoving) {
            // Handle movement and direction
            String newDirection = direction;
            if (keyH.upPressed)
                newDirection = "up";
            else if (keyH.downPressed)
                newDirection = "down";
            else if (keyH.leftPressed)
                newDirection = "left";
            else if (keyH.rightPressed)
                newDirection = "right";

            direction = newDirection;
            // Track last facing for attack direction
            if ("up".equals(newDirection))
                lastFacingDirIndex = 0;
            else if ("down".equals(newDirection))
                lastFacingDirIndex = 1;
            else if ("left".equals(newDirection))
                lastFacingDirIndex = 2;
            else if ("right".equals(newDirection))
                lastFacingDirIndex = 3;

            // Check collision first before moving
            collisionOn = false;
            gp.cChecker.checkTile(this);

            // Check for object collisions and handle them
            int objIndex = gp.cChecker.checkObject(this, true);
            pickUpObject(objIndex);

            // Check Event
            gp.eventHandler.checkEvent();

            // Check NPC collision
            int npcIndex = gp.cChecker.checkEntity(this, gp.npc);
            interactNPC(npcIndex);

            // Check monster collision
            int monsterIndex = gp.cChecker.checkEntity(this, gp.monster);
            interactMonster(monsterIndex);

            // Only move if there's no collision
            if (!collisionOn) {
                updatePosition();
            }

            updateMovementAnimation();
            resetIdleAnimation();
        } else {
            handleIdleState();
        }

        // F key: talk if near NPC (no sword attack on F)
        if (gp.gameState == gp.playState && keyH.fPressed) {
            int npcOverlapIndex = gp.cChecker.checkEntityOverlap(this, gp.npc);
            if (npcOverlapIndex == -1) {
                npcOverlapIndex = gp.cChecker.checkEntityNearby(this, gp.npc, 8);
            }
            if (npcOverlapIndex != -1) {
                interactNPC(npcOverlapIndex);
            } else {
                // No NPC nearby; just consume the F press so it doesn't linger
                keyH.fPressed = false;
            }
        }

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) { // Invincibility lasts for 60 frames (1 second)
                invincible = false; // Reset invincibility
                invincibleCounter = 0; // Reset counter
            }
        }

        // Reset hit combo if no hit landed within 3 seconds
        if (hitComboCount > 0) {
            long now = System.currentTimeMillis();
            if (now - lastHitMillis > 3000) {
                hitComboCount = 0;
            }
        }

    }

    private void updatePosition() {
        // Simple fixed movement - no speed multiplication or accumulation
        switch (direction) {
            case "up":
                worldY -= 4;
                break; // Fixed speed 4
            case "down":
                worldY += 4;
                break; // Fixed speed 4
            case "left":
                worldX -= 4;
                break; // Fixed speed 4
            case "right":
                worldX += 4;
                break; // Fixed speed 4
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
                image = attacking ? attackImages[0][attackFrameIndex] : animationImages[directionIndex][frameIndex];
                break;
            case "down":
                directionIndex = 1;
                image = attacking ? attackImages[1][attackFrameIndex] : animationImages[directionIndex][frameIndex];
                break;
            case "left":
                directionIndex = 2;
                image = attacking ? attackImages[2][attackFrameIndex] : animationImages[directionIndex][frameIndex];
                break;
            case "right":
                directionIndex = 3;
                image = attacking ? attackImages[3][attackFrameIndex] : animationImages[directionIndex][frameIndex];
                break;
            case "idle":
                if (attacking) {
                    image = attackImages[lastFacingDirIndex][attackFrameIndex];
                } else {
                    directionIndex = 4;
                    int idleFrameIndex = Math.min(idleFrame - 1, 5);
                    image = animationImages[directionIndex][idleFrameIndex];
                }
                break;
            default:
                // Default fallback
                image = animationImages[1][0]; // Down direction, first frame
                break;
        }

        // Apply invincibility transparency BEFORE drawing, and restore after
        Composite oldComposite = g2.getComposite();
        if (invincible) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }

        if (image != null) {
            // Ensure nearest-neighbor when scaling to larger size (avoid blur)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            // Zoom the player model to be 2 times larger
            int scaledSize = (int) (gp.tileSize * 2.5);
            // Draw player at center of screen
            int centerX = gp.screenWidth / 2 - scaledSize / 2;
            int centerY = gp.screenHeight / 2 - scaledSize / 2;
            g2.drawImage(image, centerX, centerY, scaledSize, scaledSize, null);
        } else {
            // Fallback: White rectangle if no image is available
            g2.setColor(Color.WHITE);
            int scaledSize = (int) (gp.tileSize * 2);
            // Draw fallback at center of screen
            int centerX = gp.screenWidth / 2 - scaledSize / 2;
            int centerY = gp.screenHeight / 2 - scaledSize / 2;
            g2.fillRect(centerX, centerY, scaledSize, scaledSize);
        }
        // Restore original composite so other draws aren't affected
        g2.setComposite(oldComposite);
        // Draw solid area for debugging (hit box)
        // g2.setColor(Color.RED);
        // int solidAreaScreenX = screenX + solidArea.x;
        // int solidAreaScreenY = screenY + solidArea.y;
        // g2.drawRect(solidAreaScreenX, solidAreaScreenY, solidArea.width,
        // solidArea.height);

    }
}
