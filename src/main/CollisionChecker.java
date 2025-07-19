package src.main;

import src.entity.Entity;

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
                    case "up":
                        entity.solidArea.y -= entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision) {
                                entity.collisionOn = true;
                            }
                            if (player) {
                                index = i;
                            }
                        }
                        break;
                    case "down":
                        entity.solidArea.y += entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision) {
                                entity.collisionOn = true;
                            }
                            if (player) {
                                index = i;
                            }
                        }
                        break;
                    case "left":
                        entity.solidArea.x -= entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision) {
                                entity.collisionOn = true;
                            }
                            if (player) {
                                index = i;
                            }
                        }
                        break;
                    case "right":
                        entity.solidArea.x += entity.speed;
                        if (entity.solidArea.intersects(gp.obj[i].solidArea)) {
                            if (gp.obj[i].collision) {
                                entity.collisionOn = true;
                            }
                            if (player) {
                                index = i;
                            }
                        }
                        break;
                
                    default:
                        break;
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

                // Check for collision
                if (entity.solidArea.intersects(targetEntities[i].solidArea)) {
                    entity.collisionOn = true;
                    index = i; // Return the index of the collided entity
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

    // Check collision between NPC and Player
    public int checkPlayer(Entity entity) {
        int index = -1; // Default return value if no collision
        
        if (gp.player != null && entity != gp.player) { // Make sure it's not the player checking itself
            // Get the NPC's solid area position after potential movement
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

            // Get the player's solid area
            gp.player.solidArea.x = gp.player.worldX + gp.player.solidAreaDefaultX;
            gp.player.solidArea.y = gp.player.worldY + gp.player.solidAreaDefaultY;

            // Check for collision
            if (entity.solidArea.intersects(gp.player.solidArea)) {
                entity.collisionOn = true;
                index = 0; // Return 0 to indicate player collision
            }

            // Reset solid area positions
            entity.solidArea.x = entity.solidAreaDefaultX;
            entity.solidArea.y = entity.solidAreaDefaultY;
            gp.player.solidArea.x = gp.player.solidAreaDefaultX;
            gp.player.solidArea.y = gp.player.solidAreaDefaultY;
        }
        
        return index; // Return 0 if collision with player, -1 if no collision
    }
}
