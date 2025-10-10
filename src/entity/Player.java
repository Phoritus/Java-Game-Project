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
import src.object.OBJ_Fireball;
import src.object.OBJ_Key;
import src.object.OBJ_Normal_Sword;
import src.object.OBJ_Premium_Shield;

public class Player extends Entity {
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;
    public boolean attackCanceled = false;
    public boolean lightUpdated = false;


    // Animation images for all directions (6 frames each)
    public BufferedImage[][] animationImages = new BufferedImage[5][6]; // [direction][frame]
    // 0=up, 1=down, 2=left, 3=right, 4=legacy idle (unused after directional idle)
    // New: directional idle frames for up/down/left/right
    public BufferedImage[][] idleImages = new BufferedImage[4][6]; // [0..3][frame]

    public int idleCounter = 0;
    public int idleFrame = 1;
    private boolean isIdle = false;
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
    // Throttle for repeating UI hints (e.g., bumping into locked door without key)
    private long lastNeedKeyMsgMillis = 0L;
    // Movement modifiers
    public int speedBonus = 0; // additive bonus to base speed (e.g., boots)
    public boolean hasBoots = false;

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);

        this.keyH = keyH;

        this.screenX = gp.screenWidth / 2 - (gp.tileSize / 2); // Center player on screen
        this.screenY = gp.screenHeight / 2 - (gp.tileSize / 2); // Center player on screen

        solidArea = new Rectangle(18, 42, 13, 18); // Larger collision area for better movement
        solidAreaDefaultX = solidArea.x; // Default X position of the solid area
        solidAreaDefaultY = solidArea.y; // Default Y position of the solid area

        // attackArea.width = 36;
        // attackArea.height = 36;

        setDefaultValues(); // Set initial position and speed
        getPlayerImage(); // Load player images (walk + idle)
        getAttackImage(); // Load attack images
        setItems(); // Initialize inventory items
        getSleepingImage(); // Load sleeping image
    }

    public BufferedImage sleepingImage;

    public void getSleepingImage() {
        sleepingImage = setup("res/objects/bed.png");
    }

    
    

    public void setDefaultValues() {
        // worldX = gp.tileSize * 12; // Initial X position - center of 50x50 map
        // worldY = gp.tileSize * 10; // Initial Y position - center of 50x50 map
        worldX = gp.tileSize * (23 + 24); // Adjusted for 100x100 map (old pos + offset)
        worldY = gp.tileSize * (14 + 25); // Adjusted for 100x100 map (old pos + offset)
        // speed = 4; // Base speed
        direction = "down"; // Default direction

        // Player status
        level = 1;
        maxLife = 6; // Maximum life points
        life = maxLife; // Start with full life
        strength = 1; // Affects attack power
        dexterity = 1; // More Dexterity, less damage taken
        exp = 0;
        ammo = 10;
        nextLevelExp = 5;
        coin = 0;
        // Inventory capacity (used by UI buy and item pickups)
        // Default matches UI grid (inventoryCols x inventoryRows = 5 x 4 = 20)
        // You can tweak at runtime if you change UI layout.
        this.maxInventorySize = 20;
        // Initialize mana so UI can render crystals
        maxMana = 4;
        mana = maxMana;
        currentWeapon = new OBJ_Normal_Sword(gp);
        currentShield = new OBJ_Premium_Shield(gp);
        projectile = new OBJ_Fireball(gp);
        // projectile = new OBJ_Rock(gp);
        attack = getAttack(); // Calculate initial attack value
        defense = getDefense(); // Calculate initial defense value
    }

    public void setDefaultPositions() {
        worldX = gp.tileSize * (23 + 24); // Adjusted for 100x100 map (old pos + offset)
        worldY = gp.tileSize * (14 + 25); // Adjusted for 100x100 map (old pos + offset)
        direction = "down"; // Default direction
        attacking = false;
        attackFrameIndex = 0;
        attackAnimCounter = 0;
        attackHitChecked = false;
        idleCounter = 0;
        idleFrame = 1;
        isIdle = false;
        lastFacingDirIndex = 1; // 0=up,1=down,2=left,3=right
    }

    public void restoreLifeAndMana() {
        life = maxLife;
        mana = maxMana;
        invincible = false;
    }

    public void setItems() {
        inventory.clear();
        inventory.add(currentWeapon);
        inventory.add(currentShield);
        // Ensure keys in starting inventory show the default key picture in inventory
        for (Entity it : inventory) {
            if (it instanceof OBJ_Key) {
                it.down1 = setup("res/objects/key/keys_1_1.png");
            }
        }
    }

    // Max inventory slots allowed for the player
    public int maxInventorySize;

    public int getAttack() {
        attackArea = currentWeapon.attackArea;
        return attack = strength * currentWeapon.attackValue;
    }

    public int getDefense() {
        return defense = dexterity * currentShield.defenseValue;
    }

    public void interactNPC(int index) {

        // Talk with F key only
        if (index != -1 && gp.keyHandler.fPressed) {
            gp.gameState = gp.dialogState; // Change game state to dialog
            gp.npc[gp.currentMap][index].speak(); // Call the speak method of the NPC
            gp.keyHandler.fPressed = false; // Reset F after successful interaction
        }
    }

    public void interactMonster(int index) {
        if (index == -1)
            return; // No monster contact
        Entity m = gp.monster[gp.currentMap][index];
        if (m == null)
            return;
        // Only apply contact damage here if the monster is alive and NOT dying,
        // and the player is not currently invincible. This prevents damage during
        // fade-out.
        if (!invincible && m.alive && !m.dying) {
            gp.playSoundEffect(7);
            int damage = m.attack - defense;
            if (damage < 0)
                damage = 0;
            life -= damage;
            invincible = true; // start i-frames
            invincibleCounter = 0;
        }
        // Note: Monsters also handle contact damage in their own update via
        // Entity.update().
        // Because player updates first, this ensures at most one damage application per
        // frame.
    }

    public void damageMonster(int index, int attack) {
        if (index < 0 || index >= gp.monster[0].length)
            return;
        Entity m = gp.monster[gp.currentMap][index];
        if (m == null)
            return;
        if (!m.alive || m.dying)
            return; // don't hit dead/dying monsters
        if (m.type != 2)
            return; // only monsters
        if (m.invincible)
            return; // already hit recently

        gp.playSoundEffect(5);
        int damage = attack - gp.monster[gp.currentMap][index].defense;
        if (damage < 0)
            damage = 0;
        m.life -= damage;
        if (m.life < 0)
            m.life = 0; // clamp to avoid negative HP bar
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
            gp.ui.addMessage("Exp +" + gp.monster[gp.currentMap][index].exp);
            m.dying = true;
            m.alive = false;
            m.invincible = false;
            exp += gp.monster[gp.currentMap][index].exp; // Gain exp from monster
            checkLevelUp();
        }
    }

    public void checkLevelUp() {

        if (exp >= nextLevelExp) {

            level++;
            nextLevelExp *= 2; // Double the experience needed for the next level
            maxLife += 2;
            maxMana += 1;
            strength++;
            dexterity++;
            life = maxLife; // Restore life on level up
            mana = maxMana; // Restore mana on level up
            attack = getAttack();
            defense = getDefense();

            gp.playSoundEffect(8);
            gp.gameState = gp.dialogState;
            gp.ui.currentDialogue = "You leveled up! You are now level " + level + "!\n"
                    + "Max Life increased by 2\n"
                    + "Mana increased by 1\n"
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
                    // Legacy idle (4) - kept for compatibility
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

            // Load new directional idle animations (up/down/left/right)
            String[][] idlePaths = {
                    // up/back idle (files named idle_back1..6)
                    { "res/player/player_idle_back/idle_back1.png", "res/player/player_idle_back/idle_back2.png",
                            "res/player/player_idle_back/idle_back3.png", "res/player/player_idle_back/idle_back4.png",
                            "res/player/player_idle_back/idle_back5.png",
                            "res/player/player_idle_back/idle_back6.png" },
                    // down/front idle (files named idle1..6)
                    { "res/player/player_idle/idle1.png", "res/player/player_idle/idle2.png",
                            "res/player/player_idle/idle3.png", "res/player/player_idle/idle4.png",
                            "res/player/player_idle/idle5.png", "res/player/player_idle/idle6.png" },
                    // left idle (files named idle_left0..5)
                    { "res/player/player_idle_left/idle_left0.png", "res/player/player_idle_left/idle_left1.png",
                            "res/player/player_idle_left/idle_left2.png", "res/player/player_idle_left/idle_left3.png",
                            "res/player/player_idle_left/idle_left4.png",
                            "res/player/player_idle_left/idle_left5.png" },
                    // right idle (files named idle_right0..5)
                    { "res/player/player_idle_right/idle_right0.png", "res/player/player_idle_right/idle_right1.png",
                            "res/player/player_idle_right/idle_right2.png",
                            "res/player/player_idle_right/idle_right3.png",
                            "res/player/player_idle_right/idle_right4.png",
                            "res/player/player_idle_right/idle_right5.png" }
            };
            for (int d = 0; d < 4; d++) {
                for (int f = 0; f < 6; f++) {
                    idleImages[d][f] = setup(idlePaths[d][f]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAttackImage() {
        try {
            // Map: up=back, down=front, left=left, right=right
            String[][] atkPaths;
            if (currentWeapon.type == TYPE_SWORD) {
                atkPaths = new String[][] {
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
            } else {
                atkPaths = new String[][] {
                        { // up (back)
                                "res/player/player_axe/b_atk/b_atk0.png",
                                "res/player/player_axe/b_atk/b_atk1.png",
                                "res/player/player_axe/b_atk/b_atk2.png",
                                "res/player/player_axe/b_atk/b_atk3.png"
                        },
                        { // down (front)
                                "res/player/player_axe/f_atk/f_atk0.png",
                                "res/player/player_axe/f_atk/f_atk1.png",
                                "res/player/player_axe/f_atk/f_atk2.png",
                                "res/player/player_axe/f_atk/f_atk3.png",
                        },
                        { // left
                                "res/player/player_axe/l_atk/l_atk0.png",
                                "res/player/player_axe/l_atk/l_atk1.png",
                                "res/player/player_axe/l_atk/l_atk2.png",
                                "res/player/player_axe/l_atk/l_atk3.png",
                        },
                        { // right
                                "res/player/player_axe/r_atk/r_atk0.png",
                                "res/player/player_axe/r_atk/r_atk1.png",
                                "res/player/player_axe/r_atk/r_atk2.png",
                                "res/player/player_axe/r_atk/r_atk3.png",
                        }
                };
            }

            for (int d = 0; d < 4; d++) {
                for (int f = 0; f < 4; f++) {
                    // Use setup() which scales with nearest-neighbor to tileSize for crisp pixels
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
        for (int i = 0; i < gp.monster[0].length; i++) {
            if (gp.monster[gp.currentMap][i] == null)
                continue;
            Rectangle monBox = new Rectangle(
                    gp.monster[gp.currentMap][i].worldX + gp.monster[gp.currentMap][i].solidAreaDefaultX,
                    gp.monster[gp.currentMap][i].worldY + gp.monster[gp.currentMap][i].solidAreaDefaultY,
                    gp.monster[gp.currentMap][i].solidArea.width,
                    gp.monster[gp.currentMap][i].solidArea.height);
            if (attackBox.intersects(monBox)) {
                damageMonster(i, attack);
                hitSomeone = true;
                // Enforce one-hit-per-attack and avoid multiple hit sounds
                break;
            }
        }

        // Break/despawn interactive tiles only if the attack box intersects them
        damageInteractiveTilesByAttack(attackBox);

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
        // Recalculate effective speed each tick: base 4 + bonus
        speed = 4 + Math.max(0, speedBonus);

        // Don't update player movement during dialogue
        if (gp.gameState == gp.dialogState) {
            // Keep idle animation during dialogue
            handleIdleState();
            isIdle = true;
            return;
        }

        boolean isMoving = keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;

        if (attacking) {
            // During attack, update attack frames and skip movement
            updateAttack();
            isIdle = false;
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

            // Debug collision bypass
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

            // Check interactive tile collision (trees, etc.). Don't destroy on movement.
            int iTileBlockIndex = gp.cChecker.checkEntity(this, gp.iTile);
            if (iTileBlockIndex != -1 && gp.iTile[gp.currentMap][iTileBlockIndex] != null
                    && gp.iTile[gp.currentMap][iTileBlockIndex].collision) {
                collisionOn = true;
            }

            // Do NOT destroy interactive tiles on movement; only attacks can break them
            // Only move if there's no collision
            if (!collisionOn) {
                updatePosition();
            }

            updateMovementAnimation();
            resetIdleAnimation();
            isIdle = false;
        } else {
            handleIdleState();
            isIdle = true;
            // Even when idle, allow pickup if overlapping an object
            int objIndexIdle = gp.cChecker.checkObject(this, true);
            pickUpObject(objIndexIdle);
            // Also process tile events while idle so F-triggered events (e.g., healing
            // pool) work without moving
            gp.eventHandler.checkEvent();
        }

        // F key: talk if near NPC (no sword attack on F)
        if (gp.gameState == gp.playState && keyH.fPressed) {
            int npcOverlapIndex = gp.cChecker.checkEntityOverlap(this, gp.npc[gp.currentMap]);
            if (npcOverlapIndex == -1) {
                npcOverlapIndex = gp.cChecker.checkEntityNearby(this, gp.npc[gp.currentMap], 8);
            }
            if (npcOverlapIndex != -1) {
                interactNPC(npcOverlapIndex);
            } else {
                // No NPC nearby; just consume the F press so it doesn't linger
                keyH.fPressed = false;
            }
        }

        if (gp.keyHandler.shortKeypress && !projectile.alive && shotAvailableCounter == 30
                && projectile.hasResource(this) == true) {
            String shootDir = direction;
            if ("idle".equals(shootDir)) {
                switch (lastFacingDirIndex) {
                    case 0:
                        shootDir = "up";
                        break;
                    case 1:
                        shootDir = "down";
                        break;
                    case 2:
                        shootDir = "left";
                        break;
                    case 3:
                    default:
                        shootDir = "right";
                        break;
                }
            }
            // Spawn from player center, then projectile applies its own directional offsets
            int baseX = worldX + gp.tileSize / 2;
            int baseY = worldY + gp.tileSize / 2;
            projectile.set(baseX, baseY, shootDir, true, this); // shoot projectile

            projectile.subtractResource(this);
            // Add projectile to the game panel's projectile list
            gp.projectileList.add(projectile);
            shotAvailableCounter = 0;

            // Shooting sound
            gp.playSoundEffect(10);
            // Consume the key so holding E doesn't spam
            gp.keyHandler.shortKeypress = false;
        }

        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) { // Invincibility lasts for 60 frames (1 second)
                invincible = false; // Reset invincibility
                invincibleCounter = 0; // Reset counter
            }
        }

        if (shotAvailableCounter < 30) {
            shotAvailableCounter++;
        }

        if (life > maxLife) {
            life = maxLife;
        }

        if (mana > maxMana) {
            mana = maxMana;
        }

        if (life <= 0) {
            gp.gameState = gp.gameOverState;
            gp.playSoundEffect(12);
            gp.stopMusic();
        }

        // Reset hit combo if no hit landed within 3 seconds
        if (hitComboCount > 0) {
            long now = System.currentTimeMillis();
            if (now - lastHitMillis > 3000) {
                hitComboCount = 0;
            }
        }

    }

    public void damageInteractiveTile(int index) {
        if (index != -1 && gp.iTile[gp.currentMap][index].destructible
                && gp.iTile[gp.currentMap][index].isCorrectItem(this)) {
            generateParticles(gp.iTile[gp.currentMap][index], gp.iTile[gp.currentMap][index]);
            gp.iTile[gp.currentMap][index] = null;
        }
    }

    // New: Only break interactive tiles if the attack hitbox actually intersects
    // them
    private void damageInteractiveTilesByAttack(Rectangle attackBox) {
        int hitIndex = -1;
        for (int i = 0; i < gp.iTile[0].length; i++) {
            if (gp.iTile[gp.currentMap][i] == null)
                continue;
            if (!gp.iTile[gp.currentMap][i].destructible)
                continue;
            if (!gp.iTile[gp.currentMap][i].isCorrectItem(this))
                continue; // e.g., require axe for trees

            Rectangle tileBox = new Rectangle(
                    gp.iTile[gp.currentMap][i].worldX + gp.iTile[gp.currentMap][i].solidAreaDefaultX,
                    gp.iTile[gp.currentMap][i].worldY + gp.iTile[gp.currentMap][i].solidAreaDefaultY,
                    gp.iTile[gp.currentMap][i].solidArea.width,
                    gp.iTile[gp.currentMap][i].solidArea.height);

            if (attackBox.intersects(tileBox)) {
                hitIndex = i;
                break; // break only one tile per swing
            }
        }

        if (hitIndex != -1 && !gp.iTile[gp.currentMap][hitIndex].invincible) {
            gp.iTile[gp.currentMap][hitIndex].playSE();
            gp.iTile[gp.currentMap][hitIndex].life--;
            // Generate visible wood chip particles at the tile
            generateParticles(gp.iTile[gp.currentMap][hitIndex], gp.iTile[gp.currentMap][hitIndex]);
            gp.iTile[gp.currentMap][hitIndex].invincible = true;
            if (gp.iTile[gp.currentMap][hitIndex].life <= 0) {
                gp.iTile[gp.currentMap][hitIndex] = gp.iTile[gp.currentMap][hitIndex].getDestroyedForm();
            }
        }
    }

    private void updatePosition() {
        // Move by current effective speed (base + bonus)
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
        // Keep the last facing direction; do not switch to a generic 'idle'
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
            Entity obj = gp.obj[gp.currentMap][index];

            // Static fixtures like houses are not pickable and should never disappear
            if (obj.type == TYPE_HOUSE) {
                return;
            }

            // Obstacles (e.g., doors): try unlocking with a key on collision
            if (obj.type == TYPE_OBSTACLE) {
                int keyIndex = -1;
                for (int i = 0; i < inventory.size(); i++) {
                    if (inventory.get(i) instanceof src.object.OBJ_Key) {
                        keyIndex = i;
                        break;
                    }
                }
                if (keyIndex != -1) {
                    inventory.remove(keyIndex);
                    gp.playSoundEffect(3); // unlock.wav
                    gp.ui.addMessage("Door unlocked!");
                    gp.obj[gp.currentMap][index] = null; // remove the door
                } else {
                    // Show this hint at most once per second while bumping the door
                    long now = System.currentTimeMillis();
                    if (now - lastNeedKeyMsgMillis >= 1000) {
                        gp.ui.addMessage("Need a key");
                        lastNeedKeyMsgMillis = now;
                    }
                }
                return;
            }

            if (obj.type == TYPE_PICKUP_ONLY) {
                obj.use(this); // Apply its effect immediately
                gp.obj[gp.currentMap][index] = null; // Remove from map
                return;
            } else {
                if (inventory.size() < maxInventorySize) {
                    // If it's a key, force its icon to the default key picture for inventory
                    if (obj instanceof OBJ_Key) {
                        obj.down1 = setup("res/objects/key/keys_1_1.png");
                    }
                    inventory.add(obj); // Keep keys (and other items) in the inventory
                    gp.playSoundEffect(1);
                    gp.ui.addMessage("Got a " + obj.name + "!");
                    gp.obj[gp.currentMap][index] = null; // Remove from map
                } else {
                    gp.ui.addMessage("You cannot carry any more items!");
                }
            }

        }
    }

    public void draw(Graphics2D g2) {
        draw(g2, gp); // Call the parent method with GamePanel parameter
    }

    public void selectItem() {
        int itemIndex = gp.ui.getItemIndexOnslot(gp.ui.playerSlotCol, gp.ui.playerSlotRow);
        if (itemIndex != -1 && itemIndex < inventory.size()) {
            Entity selectedItem = inventory.get(itemIndex);

            if (selectedItem.type == TYPE_SWORD || selectedItem.type == TYPE_AXE) {
                // Equip weapon
                currentWeapon = selectedItem;
                attack = getAttack();
                getAttackImage(); // Update attack images for new weapon

            } else if (selectedItem.type == TYPE_SHIELD) {
                // Equip shield
                currentShield = selectedItem;
                defense = getDefense();
            } else if (selectedItem.type == TYPE_CONSUMABLE) {
                // Use consumable
                selectedItem.use(this);
                inventory.remove(itemIndex);
            } if (selectedItem.type == TYPE_LIGHT) {
                if (currentLight == selectedItem) {
                    currentLight = null;
                } else {
                    currentLight = selectedItem;
                    lightRadius = currentLight.lightRadius; // Copy light radius from item
                }
                lightUpdated = true; // Trigger light radius update
            }
        }
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        BufferedImage image = null;

        // Check if player is sleeping
        if (gp.gameState == gp.sleepState && sleepingImage != null) {
            image = sleepingImage;
        } else {
            // Get current frame index based on direction and animation state
            int frameIndex = Math.min(spriteNum - 1, 5); // Ensure frame index is within bounds
            int directionIndex;

            if (attacking) {
                // Attack uses last facing direction
                image = attackImages[lastFacingDirIndex][attackFrameIndex];
            } else if (isIdle) {
                // Idle uses directional idle set by last facing
                int idleIdx = Math.min(idleFrame - 1, 5);
                image = idleImages[lastFacingDirIndex][idleIdx];
                // If any idle frame missing, gracefully fall back to walk frame
                if (image == null) {
                    int walkDir = lastFacingDirIndex;
                    image = animationImages[walkDir][frameIndex];
                }
            } else {
                // Moving: use walk cycles based on current direction
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
                    default:
                        directionIndex = 3;
                        break;
                }
                image = animationImages[directionIndex][frameIndex];
            }
        } // End of else block for normal animation

        // Apply invincibility transparency BEFORE drawing, and restore after
        Composite oldComposite = g2.getComposite();
        if (invincible) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }

        if (image != null) {
            // Ensure nearest-neighbor when scaling to larger size (avoid blur)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            
            // Use smaller size for bed sprite, normal size for player animations
            int scaledSize;
            if (gp.gameState == gp.sleepState && image == sleepingImage) {
                scaledSize = gp.tileSize; // Normal tile size for bed
            } else {
                scaledSize = (int) (gp.tileSize * 2.5); // 2.5x for player sprites
            }
            
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
