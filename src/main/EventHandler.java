package src.main;

import java.awt.Rectangle;

public class EventHandler {
    GamePanel gp; // Reference to GamePanel
    Rectangle eventRect; // Rectangle to define the area for events
    int eventRectDefaultX, eventRectDefaultY; // Default position of the event rectangle
    boolean eventTriggered = false; // Prevent multiple event triggers
    
    public EventHandler(GamePanel gp) {
        this.gp = gp;

        eventRect = new Rectangle(23, 23, 2, 2); // Full tile size for easier detection
        eventRectDefaultX = eventRect.x;
        eventRectDefaultY = eventRect.y;
    }

    public void checkEvent() {
        // Debug: Print player position
        int playerTileX = (int) Math.round((double) gp.player.worldX / gp.tileSize);
        int playerTileY = (int) Math.round((double) gp.player.worldY / gp.tileSize);
        System.out.println("Player at tile: (" + playerTileX + ", " + playerTileY + ") facing: " + gp.player.direction + " " + gp.tileSize);

        // Simple tile-based check instead of complex collision detection
        if (playerTileX == 27 && playerTileY == 14 && !eventTriggered) {
            System.out.println("Lava event triggered! Player is at (27, 14)");
            damageLava(gp.dialogState); // If player hits the event, take damage
            eventTriggered = true; // Mark event as triggered
        }
        
        // Reset event trigger when player moves away from the event tile
        if (playerTileX != 27 || playerTileY != 14) {
            eventTriggered = false;
        }

        if (playerTileX == 23 && playerTileY == 11)
        {
            if (gp.keyHandler.fPressed) { // Check if F key is pressed
                healingPool(gp.dialogState); // If player hits the event and presses F, heal
                gp.keyHandler.fPressed = false; // Reset F key state after use
            }
        }

        if (playerTileX == 26 && playerTileY == 12)
        {
            if (gp.keyHandler.tPressed) { // Check if T key is pressed
                teleportTo(gp.dialogState); // If player hits the event and presses T, teleport
                gp.keyHandler.tPressed = false; // Reset T key state after use
            }
        }
        
    }

    public boolean hitEvent(int eventCol, int eventRow, String reqDirection) {
        boolean hit = false;
        
        gp.player.solidArea.x = gp.player.worldX + gp.player.solidArea.x;
        gp.player.solidArea.y = gp.player.worldY + gp.player.solidArea.y;
        eventRect.x = eventCol * gp.tileSize + eventRectDefaultX; // Fixed: use eventRectDefaultX not eventRect.x
        eventRect.y = eventRow * gp.tileSize + eventRectDefaultY; // Fixed: use eventRectDefaultY not eventRect.y

        // Add debug output for intersection detection
        System.out.println("DEBUG: Player solid area: (" + gp.player.solidArea.x + ", " + gp.player.solidArea.y + 
                          ", " + gp.player.solidArea.width + ", " + gp.player.solidArea.height + ")");
        System.out.println("DEBUG: Event rect: (" + eventRect.x + ", " + eventRect.y + 
                          ", " + eventRect.width + ", " + eventRect.height + ")");

        // Check if player is within the event rectangle
        if (gp.player.solidArea.intersects(eventRect)) {
            System.out.println("Player intersects event area at (" + eventCol + ", " + eventRow + ")");
            // Check if the player is facing the required direction
            if (gp.player.direction.contentEquals(reqDirection) || reqDirection.contentEquals("any")) {
                hit = true; // Event hit successfully
                System.out.println("Direction requirement met: " + reqDirection);
            } else {
                System.out.println("Wrong direction. Required: " + reqDirection + ", Current: " + gp.player.direction);
            }
        }

        gp.player.solidArea.x = gp.player.solidAreaDefaultX; // Reset player solid area position
        gp.player.solidArea.y = gp.player.solidAreaDefaultY; // Reset player solid area position
        eventRect.x = eventRectDefaultX; // Reset event rectangle position
        eventRect.y = eventRectDefaultY; // Reset event rectangle position

        return hit;

    }

    public void damageLava(int gameState) {
        gp.gameState = gameState; // Change game state to damage lava
        gp.ui.currentDialogue = "You fell into lava!"; // Set dialogue for the player
        gp.player.life -= 1; // Reduce player's life by 1
        System.out.println("DAMAGE: Player life reduced from " + (gp.player.life + 1) + " to " + gp.player.life);
        
        // Prevent life from going below 0
        if (gp.player.life < 0) {
            gp.player.life = 0;
        }
    }

    public void healingPool(int gameState) {
        gp.gameState = gameState; // Change game state to healing pool
        gp.ui.currentDialogue = "You feel refreshed!"; // Set dialogue for the player
        gp.player.life = gp.player.maxLife; // Increase player's life to max
        gp.player.mana = gp.player.maxMana; // Increase player's mana to max
        // Prevent life from exceeding max life
        if (gp.player.life > gp.player.maxLife) {
            gp.player.life = gp.player.maxLife;
        }
        gp.assetSetter.setMonster();
    }

    public void teleportTo(int gameState) {
        gp.gameState = gameState; // Change game state to teleport
        gp.ui.currentDialogue = "You have been teleported!"; // Set dialogue for the player
        gp.player.worldX = 34 * gp.tileSize; 
        gp.player.worldY = 12 * gp.tileSize; 
        System.out.println("TELEPORT: Player teleported to tile (" + (gp.player.worldX / gp.tileSize) + ", " + (gp.player.worldY / gp.tileSize) + ") - World coords: (" + gp.player.worldX + ", " + gp.player.worldY + ")");
    }
}
