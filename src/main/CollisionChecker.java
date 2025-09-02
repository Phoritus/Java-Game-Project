package src.main;

import src.entity.Entity;
import java.awt.Rectangle;

public class CollisionChecker {

    GamePanel gp; // Reference to the GamePanel for accessing game settings

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {

        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftWorldX / gp.tileSize;
        int entityRightCol = entityRightWorldX / gp.tileSize;
        int entityTopRow = entityTopWorldY / gp.tileSize;
        int entityBottomRow = entityBottomWorldY / gp.tileSize;

        int tileNum1, tileNum2;

        switch (entity.direction) {
            case "up":
                entityTopRow = (entityTopWorldY - entity.speed) / gp.tileSize; // Adjust for speed
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityTopRow];
                break;
            case "down":
                entityBottomRow = (entityBottomWorldY + entity.speed) / gp.tileSize; // Adjust for speed
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityBottomRow];
                break;
            case "left":
                entityLeftCol = (entityLeftWorldX - entity.speed) / gp.tileSize; // Adjust for speed
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityLeftCol][entityBottomRow];
                break;
            case "right":
                entityRightCol = (entityRightWorldX + entity.speed) / gp.tileSize; // Adjust for speed
                tileNum1 = gp.tileManager.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityBottomRow];
                break;
        
            default:
                return;
        }

        // Check if either tile has collision enabled or is water
        if (gp.tileManager.isTileCollision(tileNum1) || gp.tileManager.isTileCollision(tileNum2) ||
            tileNum1 == 2 || tileNum2 == 2) { // Tile 2 is water - prevent walking through it
            entity.collisionOn = true;
        }

    }

    public int checkObject(Entity entity, boolean player) {
        int index = -1; // Default index if no collision occurs

        for (int i = 0; i < gp.obj.length; i++) {
            if (gp.obj[i] != null) {
                // Get the object's solid area
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;

                // Get the object's world coordinates
                gp.obj[i].solidArea.x = gp.obj[i].worldX + gp.obj[i].solidArea.x;
                gp.obj[i].solidArea.y = gp.obj[i].worldY + gp.obj[i].solidArea.y;

                switch (entity.direction) {
                    case "up": entity.solidArea.y -= entity.speed; break;
                    case "down": entity.solidArea.y += entity.speed; break;
                    case "left": entity.solidArea.x -= entity.speed; break;
                    case "right": entity.solidArea.x += entity.speed; break;
                }

                if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision) {
                                entity.collisionOn = true;
                            }
                            if (player) {
                                index = i;
                            }
                        }
                entity.solidArea.x = entity.solidAreaDefaultX; // Reset solid area position
                entity.solidArea.y = entity.solidAreaDefaultY; // Reset solid area position
                gp.obj[i].solidArea.x = gp.obj[i].solidAreaDefaultX; // Reset object's solid area position
                gp.obj[i].solidArea.y = gp.obj[i].solidAreaDefaultY;
            }
        }
        return index; // Return the index of the collided object or -1 if none
    }

    // NPC or Monster collision check
    public int checkEntity(Entity entity, Entity[] targetEntities) {
        int index = -1; // Default index if no collision occurs

        for (int i = 0; i < targetEntities.length; i++) {
            if (targetEntities[i] != null && targetEntities[i] != entity) { // Skip self-collision check
                // Skip dead or dying targets for collision/interaction
                if (!targetEntities[i].alive || targetEntities[i].dying) {
                    continue;
                }
                // Get the entity's solid area position after potential movement
                entity.solidArea.x = entity.worldX + entity.solidArea.x;
                entity.solidArea.y = entity.worldY + entity.solidArea.y;
                
                // Predict future position based on direction and speed
                switch (entity.direction) {
                    case "up":
                        entity.solidArea.y -= entity.speed;
                        break;
                    case "down":
                        entity.solidArea.y += entity.speed;
                        break;
                    case "left":
                        entity.solidArea.x -= entity.speed;
                        break;
                    case "right":
                        entity.solidArea.x += entity.speed;
                        break;
                }

                // Get the target entity's solid area
                targetEntities[i].solidArea.x = targetEntities[i].worldX + targetEntities[i].solidAreaDefaultX;
                targetEntities[i].solidArea.y = targetEntities[i].worldY + targetEntities[i].solidAreaDefaultY;

                // Check for collision/overlap
                if (entity.solidArea.intersects(targetEntities[i].solidArea)) {
                    // Only block movement if the target is collidable
                    if (targetEntities[i] != entity && targetEntities[i].collision) {
                        entity.collisionOn = true;
                    }
                    index = i; // Return the index of the overlapped entity (for interactions)
                }

                // Reset solid area positions
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                targetEntities[i].solidArea.x = targetEntities[i].solidAreaDefaultX;
                targetEntities[i].solidArea.y = targetEntities[i].solidAreaDefaultY;
            }
        }
        return index; // Return the index of the collided entity or -1 if none
    }

    // Overlap check without predicting movement (use current positions)
    public int checkEntityOverlap(Entity entity, Entity[] targetEntities) {
        int index = -1;
        for (int i = 0; i < targetEntities.length; i++) {
            if (targetEntities[i] != null && targetEntities[i] != entity) {
                // Ignore dead or dying targets for overlap checks
                if (!targetEntities[i].alive || targetEntities[i].dying) {
                    continue;
                }
                // Use current world positions with default offsets
                entity.solidArea.x = entity.worldX + entity.solidAreaDefaultX;
                entity.solidArea.y = entity.worldY + entity.solidAreaDefaultY;

                targetEntities[i].solidArea.x = targetEntities[i].worldX + targetEntities[i].solidAreaDefaultX;
                targetEntities[i].solidArea.y = targetEntities[i].worldY + targetEntities[i].solidAreaDefaultY;

                if (entity.solidArea.intersects(targetEntities[i].solidArea)) {
                    index = i;
                }

                // Reset positions
                entity.solidArea.x = entity.solidAreaDefaultX;
                entity.solidArea.y = entity.solidAreaDefaultY;
                targetEntities[i].solidArea.x = targetEntities[i].solidAreaDefaultX;
                targetEntities[i].solidArea.y = targetEntities[i].solidAreaDefaultY;
            }
        }
        return index;
    }

    // Nearby check: expands player's solid area by padding pixels and checks intersection
    public int checkEntityNearby(Entity entity, Entity[] targetEntities, int padding) {
        int index = -1;
        // Build expanded player area without mutating entity state
        Rectangle playerArea = new Rectangle(
            entity.worldX + entity.solidAreaDefaultX - padding,
            entity.worldY + entity.solidAreaDefaultY - padding,
            entity.solidArea.width + padding * 2,
            entity.solidArea.height + padding * 2
        );

        for (int i = 0; i < targetEntities.length; i++) {
            if (targetEntities[i] != null && targetEntities[i] != entity) {
                if (!targetEntities[i].alive || targetEntities[i].dying) {
                    continue;
                }
                Rectangle targetArea = new Rectangle(
                    targetEntities[i].worldX + targetEntities[i].solidAreaDefaultX,
                    targetEntities[i].worldY + targetEntities[i].solidAreaDefaultY,
                    targetEntities[i].solidArea.width,
                    targetEntities[i].solidArea.height
                );

                if (playerArea.intersects(targetArea)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    // Check collision between NPC/Monster and Player
    public boolean checkPlayer(Entity entity) {
        boolean contactPlayer = false;

        if (gp.player != null && entity != gp.player) { // Make sure it's not the player checking itself
            // If the entity is dead or dying (e.g., fading out), it shouldn't hurt or block the player
            if (!entity.alive || entity.dying) {
                return false;
            }
            // Position rectangles in world space using default offsets
            entity.solidArea.x = entity.worldX + entity.solidAreaDefaultX;
            entity.solidArea.y = entity.worldY + entity.solidAreaDefaultY;

            gp.player.solidArea.x = gp.player.worldX + gp.player.solidAreaDefaultX;
            gp.player.solidArea.y = gp.player.worldY + gp.player.solidAreaDefaultY;

            // Predict future position based on movement this frame
            switch (entity.direction) {
                case "up": entity.solidArea.y -= entity.speed; break;
                case "down": entity.solidArea.y += entity.speed; break;
                case "left": entity.solidArea.x -= entity.speed; break;
                case "right": entity.solidArea.x += entity.speed; break;
            }

            // Check for collision
            if (entity.solidArea.intersects(gp.player.solidArea)) {
                contactPlayer = true;
                entity.collisionOn = true; // also block movement if desired
            }

            // Reset solid area positions
            entity.solidArea.x = entity.solidAreaDefaultX;
            entity.solidArea.y = entity.solidAreaDefaultY;
            gp.player.solidArea.x = gp.player.solidAreaDefaultX;
            gp.player.solidArea.y = gp.player.solidAreaDefaultY;
        }

        return contactPlayer;
    }
}
