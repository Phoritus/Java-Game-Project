package src.monster;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.image.BufferedImage;

import src.data.Progress;
import src.entity.Entity;
import src.main.GamePanel;
import src.object.OBJ_IronDoor;

public class MonMinotour extends Entity {

  private static final double SCALE = 9.0; // Boss is 9x larger than normal tile (increased from 7)
  private int localIdleCounter = 0;
  boolean hpBarOn = true; // Always show HP bar for boss
  int hpBarCounter = 0;

  // Debug display
  private double distanceToPlayer = 0;
  
  // Track last facing direction for idle animation
  private String lastDirection = "right";

  // Animation speed control
  private final int WALK_ANIMATION_SPEED = 8; // Speed for 12-frame walk animations (higher = slower)
  private final int IDLE_ANIMATION_SPEED = 3; // Speed for 16-frame idle animations (higher = slower)
  private final int ATTACK_ANIMATION_SPEED = 5; // Speed for 16-frame attack animations (higher = slower)

  // Attack system
  private boolean isAttacking = false;
  private int attackCounter = 0;
  private int attackAnimationIndex = 0;
  private boolean attackHitChecked = false;
  private int attackCooldown = 0;
  private final int attackRange = 3; // Range in tiles
  private BufferedImage[] attackLeftImages = new BufferedImage[16]; // Increased to 16 frames
  private BufferedImage[] attackRightImages = new BufferedImage[16]; // Increased to 16 frames

  // Health bar components
  private BufferedImage healthBarUnder;    // Background layer
  private BufferedImage healthBarProgress; // HP progress layer (will be clipped)
  private BufferedImage healthBarOver;     // Top frame/overlay layer

  // AI tracking
  private boolean aggro = false;
  private int visionRange = 7;  // Can see player 7 tiles away

  public static final String monName = "Minotour";

  public MonMinotour(GamePanel gp) {
    super(gp);

    type = TYPE_MONSTER;
    name = monName;
    speed = 1;
    maxLife = 10;
    life = maxLife;
    attack = 10;
    defense = 5;
    exp = 50;
    direction = "right";
    sleep = true;

    // Collision box for large entity (7x tile size)
    // Much smaller hitbox centered on the body/legs only
    int size = (int) (gp.tileSize * SCALE);
    solidArea.x = (int)(size * 0.35); // Center horizontally (35% from left)
    solidArea.y = (int)(size * 0.48);  // Start at middle (48% from top)
    solidArea.width = (int)(size * 0.3); // Only 30% width for tight hitbox
    solidArea.height = (int)(size * 0.4); // Only 40% height for legs/lower body
    solidAreaDefaultX = solidArea.x;
    solidAreaDefaultY = solidArea.y;

    // Attack area (larger for boss)
    attackArea.width = size;
    attackArea.height = size;

    motion1_duration = 30; // Attack animation duration
    motion2_duration = 60; // Attack cooldown duration

    loadImages();
    setDialogue();
  }


  private void loadImages() {
    int size = (int) (gp.tileSize * SCALE);

    // Load walk left animation - 12 frames (walk_1.png to walk_12.png)
    // Stored in animationImages[2]
    for (int i = 0; i < 12; i++) {
      String path = "/res/monster/boss/walk_left/walk_" + (i + 1) + ".png";
      animationImages[2][i] = setup(path, size, size);
    }

    // Load walk right animation - 12 frames (walk_00.png to walk_11.png)
    // Stored in animationImages[3]
    for (int i = 0; i < 12; i++) {
      String path = String.format("/res/monster/boss/walk_right/walk_%02d.png", i);
      animationImages[3][i] = setup(path, size, size);
    }

    // Load idle left animation - 16 frames (idle_1.png to idle_16.png)
    // Stored in animationImages[0]
    for (int i = 0; i < 16; i++) {
      String path = "/res/monster/boss/idle_left/idle_" + (i + 1) + ".png";
      animationImages[0][i] = setup(path, size, size);
    }

    // Load idle right animation - 16 frames (idle_00.png to idle_15.png)
    // Stored in animationImages[1]
    for (int i = 0; i < 16; i++) {
      String path = String.format("/res/monster/boss/idle_right/idle_%02d.png", i);
      animationImages[1][i] = setup(path, size, size);
    }

    // Load attack left animation - 16 frames (atk_1_1.png to atk_1_16.png)
    for (int i = 0; i < 16; i++) {
      String path = "/res/monster/boss/atk_left/atk_1_" + (i + 1) + ".png";
      attackLeftImages[i] = setup(path, size, size);
    }

    // Load health bar components
    healthBarUnder = setup("/res/monster/boss/health_bar/mino_health_under.png", size, (int)(size * 0.15));
    healthBarProgress = setup("/res/monster/boss/health_bar/mino_health_progress.png", size, (int)(size * 0.15));
    healthBarOver = setup("/res/monster/boss/health_bar/mino_health_over.png", size, (int)(size * 0.15));

    // Load attack right animation - 16 frames (atk_1_00.png to atk_1_15.png)
    for (int i = 0; i < 16; i++) {
      String path = String.format("/res/monster/boss/atk_right/atk_1_%02d.png", i);
      attackRightImages[i] = setup(path, size, size);
    }
  }

  public void setDialogue() {
    dialogues[0][0] = "Grrrr... Who dares enter\nmy domain?";
    dialogues[0][1] = "You will regret this\nintrusion, human!";
    dialogues[0][2] = "Feel my wrath!";

  }

  public void checkDrop() {
    // Boss does not drop items
    gp.bossBattleOn = false;
    Progress.minotourDefeated = true;

    // Restore the previous background music
    gp.stopMusic();
    gp.playMusic(17); // Play boss fight music

    // Remove iron doors (boss is on map 3)
    for (int i = 0; i < gp.obj[3].length; i++) {
      if (gp.obj[3][i] != null && gp.obj[3][i].name.equals(new OBJ_IronDoor(gp).name)) {
        gp.playSoundEffect(3);
        gp.obj[3][i] = null;
      }
    }
  }

  // Check if player is within vision range
  private boolean canSeePlayer() {
    if (gp == null || gp.player == null) return false;

    int bossCenterX = worldX + (int)(gp.tileSize * SCALE / 2);
    int bossCenterY = worldY + (int)(gp.tileSize * SCALE / 2);
    int playerCenterX = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2;
    int playerCenterY = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;

    double dx = bossCenterX - playerCenterX;
    double dy = bossCenterY - playerCenterY;
    double distance = Math.sqrt(dx * dx + dy * dy);
    double visionDistance = visionRange * gp.tileSize;

    return distance <= visionDistance;
  }

  private boolean isPlayerInAttackRange() {
    if (gp == null || gp.player == null) return false;

    int bossSize = (int)(gp.tileSize * SCALE);
    int bossX = worldX;
    int bossY = worldY;
    int playerX = gp.player.worldX + gp.player.solidArea.x;
    int playerY = gp.player.worldY + gp.player.solidArea.y;
    int expandedRange = gp.tileSize * attackRange;

    return playerX + gp.player.solidArea.width > bossX - expandedRange &&
           playerX < bossX + bossSize + expandedRange &&
           playerY + gp.player.solidArea.height > bossY - expandedRange &&
           playerY < bossY + bossSize + expandedRange;
  }

  private void steerTowardsPlayer() {
    if (gp == null || gp.player == null) return;

    int dx = gp.player.worldX - worldX;
    int dy = gp.player.worldY - worldY;

    if (Math.abs(dx) > Math.abs(dy)) {
      direction = dx < 0 ? "left" : "right";
      lastDirection = direction; // Update lastDirection only for left/right
    } else {
      direction = dy < 0 ? "up" : "down";
      // DON'T update lastDirection for up/down - keep previous left/right
    }
  }

  @Override
  public void setAction() {
    if (isAttacking) return;

    if (canSeePlayer()) {
      aggro = true;
      steerTowardsPlayer(); // อัปเดตทุกเฟรม

      if (isPlayerInAttackRange() && attackCooldown == 0) {
        startAttack();
        return;
      }
    } else {
      aggro = false;
      direction = "idle";
    }
  }

  private void startAttack() {
    isAttacking = true;
    attackCounter = 0;
    attackAnimationIndex = 0;
    attackHitChecked = false;

    // Face player and update lastDirection for idle animation after attack
    if (gp != null && gp.player != null) {
      int dx = gp.player.worldX - worldX;
      
      // Always face the direction where player is located
      if (dx < 0) {
        // Player is on the LEFT side
        direction = "left";
        lastDirection = "left";
      } else {
        // Player is on the RIGHT side (or directly above/below)
        direction = "right";
        lastDirection = "right";
      }
    }
  }

  @Override
  public void update() {
    // Calculate distance to player for debug
    if (gp != null && gp.player != null) {
      int dx = Math.abs(gp.player.worldX - worldX);
      int dy = Math.abs(gp.player.worldY - worldY);
      distanceToPlayer = Math.sqrt(dx * dx + dy * dy) / gp.tileSize;
    }
    
    // Track last direction for idle animation (only update for left/right)
    if (!"idle".equals(direction) && ("left".equals(direction) || "right".equals(direction))) {
      lastDirection = direction;
    }

    // Handle attack animation
    if (isAttacking) {
      updateAttack();
    } else {
      // Custom update for walk animation speed control
      setAction();
      
      collisionOn = false;
      gp.cChecker.checkTile(this);
      gp.cChecker.checkObject(this, false);
      gp.cChecker.checkEntity(this, gp.npc);
      gp.cChecker.checkEntity(this, gp.monster);
      boolean contactPlayer = gp.cChecker.checkPlayer(this);
      
      if (this.type == TYPE_MONSTER && contactPlayer == true) {
        damagePlayer(attack);
      }
      
      if (collisionOn == false) {
        switch (direction) {
          case "up": worldY -= speed; break;
          case "down": worldY += speed; break;
          case "left": worldX -= speed; break;
          case "right": worldX += speed; break;
        }
      }
      
      // Custom frame advancement with WALK_ANIMATION_SPEED
      spriteCounter++;
      if (spriteCounter > WALK_ANIMATION_SPEED) {
        spriteCounter = 0;
        frameIndex++;
        if (frameIndex >= 12) { // Walk animations have 12 frames
          frameIndex = 0;
        }
      }
      
      if (this.invincible) {
        invincibleCounter++;
        if (invincibleCounter > 40) {
          invincible = false;
          invincibleCounter = 0;
        }
      }
    }

    // If dead/dying, keep showing HP bar but cancel attack
    if (!alive || this.dying) {
      // Don't hide HP bar for boss - keep it visible
      isAttacking = false;
    }

    // Handle idle animation (16 frames)
    if ("idle".equals(direction)) {
      localIdleCounter++;
      if (localIdleCounter > IDLE_ANIMATION_SPEED) {
        localIdleCounter = 0;
        idleFrame++;
        if (idleFrame > 16) // Loop through 16 frames
          idleFrame = 1;
      }
    } else {
      localIdleCounter = 0;
      idleFrame = 1;
    }

    // HP bar always visible for boss - no timer needed
    // (Removed auto-hide timer)

    // Attack cooldown
    if (attackCooldown > 0) {
      attackCooldown--;
    }
  }

  private void updateAttack() {
    attackCounter++;

    // Change attack frame based on ATTACK_ANIMATION_SPEED
    if (attackCounter % ATTACK_ANIMATION_SPEED == 0) {
      attackAnimationIndex++;
    }

    // Check for hit at frame 8 (middle of attack animation)
    if (attackAnimationIndex == 8 && !attackHitChecked) {
      checkAttackHit();
      attackHitChecked = true;
    }

    // End attack after all 16 frames
    if (attackAnimationIndex >= 16) {
      isAttacking = false;
      attackCounter = 0;
      attackAnimationIndex = 0;
      attackCooldown = motion2_duration; // Set cooldown using motion2_duration
    }
  }

  private void checkAttackHit() {
    if (gp == null || gp.player == null)
      return;

    // Use circular attack range instead of rectangle
    int bossSize = (int) (gp.tileSize * SCALE);
    
    // Calculate boss center
    int bossCenterX = worldX + bossSize / 2;
    int bossCenterY = worldY + bossSize / 2;
    
    // Calculate player center
    int playerCenterX = gp.player.worldX + gp.player.solidArea.x + gp.player.solidArea.width / 2;
    int playerCenterY = gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height / 2;
    
    // Calculate distance between centers
    double dx = bossCenterX - playerCenterX;
    double dy = bossCenterY - playerCenterY;
    double distance = Math.sqrt(dx * dx + dy * dy);
    
    // Attack range is half of boss size (radius of the sprite)
    double attackRadius = bossSize / 2.0;
    
    // Hit if player center is within attack radius
    if (distance <= attackRadius) {
      // Hit the player!
      damagePlayer(attack);
      gp.playSoundEffect(5); // Play hit sound
    }
  }

  public void showHpBar() {
    hpBarOn = true;
    hpBarCounter = 0;
  }

  @Override
  public void draw(Graphics2D g2, GamePanel gp) {
    // Calculate screen position
    screenX = worldX - gp.player.worldX + gp.player.screenX;
    screenY = worldY - gp.player.worldY + gp.player.screenY;

    int size = (int) (gp.tileSize * SCALE);

    // Only draw if visible (check with large entity size)
    if (worldX + size > gp.player.worldX - gp.player.screenX &&
        worldX - size < gp.player.worldX + gp.player.screenX &&
        worldY + size > gp.player.worldY - gp.player.screenY &&
        worldY - size < gp.player.worldY + gp.player.screenY) {

      BufferedImage image = null;

      // If attacking, use attack animation
      if (isAttacking) {
        if ("left".equals(direction)) {
          image = attackLeftImages[Math.min(attackAnimationIndex, 15)]; // 16 frames (0-15)
        } else {
          image = attackRightImages[Math.min(attackAnimationIndex, 15)]; // 16 frames (0-15)
        }
      } else {
        // Normal movement/idle animation
        int directionIndex;
        int maxFrame;
        switch (direction) {
          case "left":
            directionIndex = 2; // walk left
            maxFrame = 11; // 12 frames (0-11)
            break;
          case "right":
            directionIndex = 3; // walk right
            maxFrame = 11; // 12 frames (0-11)
            break;
          case "up":
          case "down":
            // Boss doesn't have up/down animations, use left/right based on lastDirection
            if (lastDirection != null && "left".equals(lastDirection)) {
              directionIndex = 2; // walk left animation
            } else {
              directionIndex = 3; // walk right animation
            }
            maxFrame = 11; // 12 frames (0-11)
            break;
          case "idle":
          default:
            // Use idle animation based on last facing direction
            if (lastDirection != null && "left".equals(lastDirection)) {
              directionIndex = 0; // idle left (ตามทิศทางที่หันล่าสุด)
            } else {
              directionIndex = 1; // idle right (ตามทิศทางที่หันล่าสุด)
            }
            maxFrame = 15; // 16 frames (0-15)
            break;
        }

        int frame = ("idle".equals(direction)) ? Math.min(idleFrame - 1, maxFrame) : Math.min(this.frameIndex, maxFrame);
        if (animationImages[directionIndex][frame] != null) {
          image = animationImages[directionIndex][frame];
        }
      }

      // HP Bar for boss using layered components
      if (hpBarOn && healthBarUnder != null && healthBarProgress != null && healthBarOver != null) {
        // Fixed size health bar (not scaled with boss size)
        int barWidth = 300; // Shorter width
        int barHeight = 50; // Reduced height
        int barX = screenX + (size - barWidth) / 2; // Center horizontally on boss
        int barY = screenY + 70; // Position near head (adjust this value: smaller = higher, larger = lower)

        // Calculate HP percentage
        double hpPercentage = (double) life / maxLife;

        // Layer 1: Draw background (under)
        g2.drawImage(healthBarUnder, barX, barY, barWidth, barHeight, null);

        // Layer 2: Draw HP progress (clipped to current HP percentage)
        if (hpPercentage > 0) {
          // Get actual image dimensions
          int imgWidth = healthBarProgress.getWidth();
          int imgHeight = healthBarProgress.getHeight();
          
          // Calculate how much of the source image to show based on HP
          int srcClipWidth = (int) (imgWidth * hpPercentage);
          int destClipWidth = (int) (barWidth * hpPercentage);
          
          // Draw only the HP portion (clip both source and destination)
          g2.drawImage(healthBarProgress, 
                       barX, barY,                          // destination top-left
                       barX + destClipWidth, barY + barHeight,  // destination bottom-right (clipped by HP%)
                       0, 0,                                // source top-left
                       srcClipWidth, imgHeight,             // source bottom-right (clipped by HP%)
                       null);
        }

        // Layer 3: Draw overlay frame (over) - always full size
        g2.drawImage(healthBarOver, barX, barY, barWidth, barHeight, null);
      }

      // Draw boss with proper scaling
      if (image != null) {
        Composite old = g2.getComposite();

        if (this.dying) {
          // Fade out when dying
          float alpha = 1.0f - Math.min(1.0f, dyingCounter / 40.0f);
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.0f, alpha)));
        } else if (this.invincible) {
          // Flash when invincible
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        g2.drawImage(image, screenX, screenY, size, size, null);
        g2.setComposite(old);
      }

      // Debug display
      if (gp.keyHandler.bossDebug) {

        g2.setColor(Color.YELLOW);
        g2.drawString(String.format("Distance: %.1f tiles", distanceToPlayer), screenX, screenY + size + 15);
        g2.drawString(String.format("Attack Range: %d tiles", attackRange), screenX, screenY + size + 30);
        g2.drawString("Aggro: " + aggro, screenX, screenY + size + 45);
        g2.drawString("Attacking: " + isAttacking, screenX, screenY + size + 60);
        g2.drawString("Cooldown: " + attackCooldown, screenX, screenY + size + 75);

        // Draw full sprite bounds (white outline)
        g2.setColor(new Color(255, 255, 255, 50));
        g2.drawRect(screenX, screenY, size, size);

        // Draw collision box (RED - this is what blocks movement)
        g2.setColor(new Color(255, 0, 0, 150));
        g2.fillRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);
        g2.setColor(Color.RED);
        g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);
        
        // Draw attack hitbox when attacking (ORANGE CIRCLE - circular attack range)
        if (isAttacking) {
          // Attack hitbox is a circle (radius = half of sprite size)
          g2.setColor(new Color(255, 165, 0, 100));
          g2.fillOval(screenX, screenY, size, size);
          g2.setColor(Color.ORANGE);
          g2.drawOval(screenX, screenY, size, size);
        }

        // Draw attack range circle (YELLOW - when player enters, boss attacks)
        g2.setColor(new Color(255, 255, 0, 50));
        int attackRangePixels = attackRange * gp.tileSize;
        g2.drawOval(screenX + size / 2 - attackRangePixels,
            screenY + size / 2 - attackRangePixels,
            attackRangePixels * 2,
            attackRangePixels * 2);

        // Draw vision range circle (GREEN - when player enters, boss chases)
        g2.setColor(new Color(0, 255, 0, 30));
        int visionRangePixels = visionRange * gp.tileSize;
        g2.drawOval(screenX + size / 2 - visionRangePixels,
            screenY + size / 2 - visionRangePixels,
            visionRangePixels * 2,
            visionRangePixels * 2);
      }
    }
  }

  @Override
  public void damageReaction() {
    actionLockCounter = 0;
    aggro = true; // Become aggressive when hit
    showHpBar(); // Show HP bar when damaged
  }

}
