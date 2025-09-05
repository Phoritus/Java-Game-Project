package src.main;

public class EventHandler {
    GamePanel gp; // Reference to GamePanel
    EventRect eventRect[][][];
    private boolean eventTriggered = false;
    boolean canTouchEvent = true;
    int previousEventX, previousEventY;  
    int tempMap, tempCol, tempRow;

    public EventHandler(GamePanel gp) {
        this.gp = gp;
        // Use world dimensions (not screen rows) and clear nested loops
        eventRect = new EventRect[gp.maxMap][gp.maxWorldCol][gp.maxWorldRow];
        for (int map = 0; map < gp.maxMap; map++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                for (int row = 0; row < gp.maxWorldRow; row++) {
                    EventRect er = new EventRect();
                    er.x = col * gp.tileSize;
                    er.y = row * gp.tileSize;
                    er.width = gp.tileSize;
                    er.height = gp.tileSize;
                    er.eventRectDefaultX = er.x;
                    er.eventRectDefaultY = er.y;
                    eventRect[map][col][row] = er;
                }
            }
        }
    }

    public void checkEvent() {
        // Healing pool should work even when standing still; check before gating
        if (gp.gameState == gp.playState && gp.keyHandler.fPressed && gp.currentMap == 0) {
            int centerX = gp.player.worldX + gp.player.solidAreaDefaultX + gp.player.solidArea.width / 2;
            int centerY = gp.player.worldY + gp.player.solidAreaDefaultY + gp.player.solidArea.height / 2;
            int colNow = Math.max(0, Math.min(gp.maxWorldCol - 1, centerX / gp.tileSize));
            int rowNow = Math.max(0, Math.min(gp.maxWorldRow - 1, centerY / gp.tileSize));
            // Accept a small vertical band around the spark (rows 12-13)
            if (colNow == 23 && (rowNow == 12 || rowNow == 13)) {
                healingPool(gp.dialogState);
                gp.keyHandler.fPressed = false; // consume press so it fires once per tap
                previousEventX = gp.player.worldX;
                previousEventY = gp.player.worldY;
                // don't return; allow other events logic to proceed if needed
            }
        }

        // Check if player is more than 1 tile away from last event
        int xDistance = Math.abs(gp.player.worldX - previousEventX);
        int yDistance = Math.abs(gp.player.worldY - previousEventY);
        int distance = Math.max(xDistance, yDistance);
        if (distance > gp.tileSize) {
            canTouchEvent = true;
        }

        if (canTouchEvent) {
            // Healing pool handled above using center tile + F; keep other events below

            // Example event: Teleportation event at tile (26,12) on map 0
            if (hitEvent(0, 26, 12, "any")) {
                if (gp.keyHandler.tPressed) { // Check if T key is pressed
                    teleport(1, 34, 12); // If player hits the event and presses T, teleport
                    gp.keyHandler.tPressed = false; // Reset T key state after use
                }
            }
            else if (hitEvent(0, 13, 39, "any")) {
                teleport(1, 12, 11);
                gp.playSoundEffect(13); // tele1.wav
            }
            else if (hitEvent(1, 12, 13, "any")) {
                teleport(0, 13, 39);
                gp.playSoundEffect(14); // tele2.wav
            }
            // Talking to NPCs is handled via Player.interactNPC on F press; no tile-triggered talk here.
            // If an event was triggered, prevent immediate retriggering
            if (eventTriggered) {
                canTouchEvent = false;
                eventTriggered = false;
                previousEventX = gp.player.worldX;
                previousEventY = gp.player.worldY;
            }
        }

    }

    public boolean hitEvent(int map, int eventCol, int eventRow, String reqDirection) {
        // Bounds check indices and current map layer
        if (map != gp.currentMap) return false;
        if (eventCol < 0 || eventCol >= gp.maxWorldCol || eventRow < 0 || eventRow >= gp.maxWorldRow) return false;

        // Build player solid area in world space without mutating defaults
        java.awt.Rectangle playerBox = new java.awt.Rectangle(
            gp.player.worldX + gp.player.solidAreaDefaultX,
            gp.player.worldY + gp.player.solidAreaDefaultY,
            gp.player.solidArea.width,
            gp.player.solidArea.height
        );

        EventRect er = eventRect[map][eventCol][eventRow];
        java.awt.Rectangle eventBox = new java.awt.Rectangle(er.x, er.y, er.width, er.height);

        boolean intersects = playerBox.intersects(eventBox) && !er.eventDone;
        if (!intersects) return false;

        if (!"any".equalsIgnoreCase(reqDirection) && !gp.player.direction.equalsIgnoreCase(reqDirection)) {
            return false;
        }
        eventTriggered = true;
        return true;
    }

    public void damageLava(int gameState) {
        gp.gameState = gameState; // Change game state to damage lava
        gp.player.life -= 1; // Reduce player's life by 1
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

    public void teleport(int map, int col, int row) {
        gp.gameState = gp.transitionState;
        tempMap = map;
        tempCol = col;
        tempRow = row;
        canTouchEvent = false;
    }

    // Removed redundant speak(Entity) helper; Player.interactNPC handles talking uniformly.
}
