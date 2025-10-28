package src.main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

import src.entity.Entity;
import src.object.OBJ_Heart;
import src.object.OBJ_ManaCrystal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

public class UI {

    GamePanel gp; // Reference to the GamePanel for accessing game settings
    Font Minecraft; // Fonts for displaying text
    Graphics2D g2; // Graphics context for drawing
    public boolean messageOn = false; // Flag to control message display
    ArrayList<String> messages = new ArrayList<>(); // List of messages to display
    ArrayList<Integer> messageCounters = new ArrayList<>(); // Corresponding counters for each message
    public boolean gameFinished = false; // Flag to indicate if the game is finished
    public String currentDialogue = ""; // Current dialogue text to display
    public int commandNumber = 0; // Command number for dialogue or actions

    // Animation variables for title screen
    public int titleAnimationCounter = 0;
    public int titleCurrentFrame = 0;
    public int titleAnimationSpeed = 1; // Slower animation for title screen
    public int playerSlotCol = 0;
    public int playerSlotRow = 0;
    public int npcSlotCol = 0;
    public int npcSlotRow = 0;
    int counter = 0;
    int subState = 0;
    public Entity npc;
    public int charIndex = 0;
    public String combinedText = "";
    public int typewriterCounter = 0; // Counter for typewriter effect speed
    // Defer applying fullscreen toggle until confirmation/notice screen
    boolean pendingFullScreenToggle = false;

    // Inventory grid config
    public int inventoryCols = 5; // columns in inventory grid
    public int inventoryRows = 4; // rows in inventory grid

    // Remember last-drawn inventory frame positions for alignment
    public int playerInvFrameX, playerInvFrameY, playerInvFrameW, playerInvFrameH;
    public int npcInvFrameX, npcInvFrameY, npcInvFrameW, npcInvFrameH;

    BufferedImage heart_full, heart_half, heart_blank, crystal_full, crystal_blank, coin; // Heart images for health
                                                                                          // display

    public UI(GamePanel gp) {
        this.gp = gp; // Initialize the GamePanel reference
        try {
            InputStream is = getClass().getResourceAsStream("/res/font/Minecraft.ttf");
            Minecraft = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(40f); // Load Minecraft font
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions, such as file not found
        }
        // Create heart images for health display
        Entity heart = new OBJ_Heart(gp);
        heart_full = heart.image; // Full heart image
        heart_half = heart.image2; // Half heart image
        heart_blank = heart.image3; // Blank heart image

        Entity crystal = new OBJ_ManaCrystal(gp);
        crystal_full = crystal.image; // Full crystal image
        crystal_blank = crystal.image2; // Blank crystal

        Entity GoldCoin = new src.object.OBJ_Coin(gp);
        coin = GoldCoin.down1;

    }

    public void addMessage(String text) {
        messages.add(text);
        messageCounters.add(0);
        messageOn = true;
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;

        g2.setFont(Minecraft);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.white); // Set text color to white

        // Title state
        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
        }

        // Game Over state
        if (gp.gameState == gp.gameOverState) {
            drawGameOverScreen();
        }

        if (gp.gameState == gp.playState) {
            drawPlayerHealth();
            drawMessages();
        } else if (gp.gameState == gp.pauseState) {
            drawPlayerHealth();
            drawPauseScreen();

        }

        // Dialog state
        if (gp.gameState == gp.dialogueState) {
            drawPlayerHealth();
            if (npc != null) {
                drawDialogueScreen();
            }
        }

        // Cutscene state
        if (gp.gameState == gp.cutsceneState) {
            // Do not draw HUD during cutscenes
            if (npc != null) {
                drawDialogueScreen();
            }
        }

        // Character customization state
        if (gp.gameState == gp.characterState) {
            drawCharacterScreen();
            drawInventory(gp.player, true);
        }

        // Option state
        if (gp.gameState == gp.optionState) {
            drawOptionScreen();
        }

        // Transition state
        if (gp.gameState == gp.transitionState) {
            drawTransition();
        }

        // Trade state
        if (gp.gameState == gp.tradeState) {
            drawTradeScreen();
        }

        // Sleep state
        if (gp.gameState == gp.sleepState) {
            drawSleepScreen();
        }
    }

    public void drawTradeScreen() {
        switch (subState) {
            case 0:
                trade_select();
                break;
            case 1:
                trade_buy();
                break;
            case 2:
                trade_sell();
                break;
        }
        gp.keyHandler.fPressed = false;
    }

    public void trade_select() {
        drawDialogueScreen();

        // Draw window
        int x = gp.tileSize * 15;
        int y = gp.tileSize * 5;
        int width = gp.tileSize * 3;
        int height = (int) (gp.tileSize * 3.5);
        drawSubWindow(x, y, width, height);

        // Draw texts
        x += gp.tileSize - 10;
        y += gp.tileSize;
        g2.setFont(g2.getFont().deriveFont(20f));
        g2.drawString("Buy", x, y);
        if (commandNumber == 0) {
            g2.drawString(">", x - 20, y);
        }
        y += gp.tileSize;
        g2.drawString("Sell", x, y);
        if (commandNumber == 1) {
            g2.drawString(">", x - 20, y);
        }
        y += gp.tileSize;
        g2.drawString("Leave", x, y);
        if (commandNumber == 2) {
            g2.drawString(">", x - 20, y);
        }
    }

    public void trade_buy() {
        // Draw playr inventory with cursor
        drawInventory(gp.player, false);

        // Draw NPC inventory without cursor
        drawInventory(npc, true);

        // Align helper windows directly under the hovered slot for each inventory
        int spacing = gp.tileSize * 3; // vertical gap below the slots

        // Hint window under NPC inventory (left)
        int x = npcInvFrameX;
        int y = npcInvFrameY + npcInvFrameH + spacing;
        int width = npcInvFrameW;
        int height = gp.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("[ESC] Back", x + 24, y + 60);

        // Player coin window under Player inventory (right)
        x = playerInvFrameX;
        y = playerInvFrameY + playerInvFrameH + spacing;
        width = playerInvFrameW;
        height = gp.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("Coins: " + gp.player.coin, x + 24, y + 60);

        // Draw Price window
        int itemIndex = getItemIndexOnslot(npcSlotCol, npcSlotRow);
        if (itemIndex != -1 && itemIndex < npc.inventory.size()) {
            x = (int) (gp.tileSize * 5.5);
            y = (int) (gp.tileSize * 5.5);
            width = (int) (gp.tileSize * 2.5);
            height = gp.tileSize;
            drawSubWindow(x, y, width, height);
            g2.drawImage(coin, x + 10, y + 10, 28, 28, null);

            int price = npc.inventory.get(itemIndex).price;
            String text = String.valueOf(price);
            x = getXforAlignToRightText(text, gp.tileSize * 8);
            g2.drawString(text, x - 19, y + 32);

            // Buy an item (Enter or F → enterPressed)
            if (gp.keyHandler.enterPressed) {
                if (npc.inventory.get(itemIndex).price > gp.player.coin) {
                    // Not enough coin - NPC dialogue set 1, index 0
                    subState = 0;
                    npc.startDialogue(npc, 1);
                    npc.dialogueIndex = 0;
                } else if (gp.player.inventory.size() == gp.player.maxInventorySize) {
                    // Inventory full - NPC dialogue set 1, index 1
                    subState = 0;
                    npc.startDialogue(npc, 1);
                    npc.dialogueIndex = 1;
                } else {
                    // Purchase successful
                    gp.player.coin -= npc.inventory.get(itemIndex).price;
                    gp.player.inventory.add(npc.inventory.get(itemIndex));
                }
                // consume
                gp.keyHandler.enterPressed = false;
            }
        }

    }

    public void trade_sell() {
        // Compute frames (including NPC frame) without drawing NPC items
        computeInventoryFrames();

        // Draw player inventory with cursor
        drawInventory(gp.player, true);

        // Align helper windows directly under the hovered slot for each inventory
        int spacing = gp.tileSize * 3; // vertical gap below the slots

        // Hint window aligned under NPC inventory frame (same as trade_buy),
        // while NPC inventory itself remains hidden
        int x = npcInvFrameX;
        int y = npcInvFrameY + npcInvFrameH + spacing;
        int width = npcInvFrameW;
        int height = gp.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("[ESC] Back", x + 24, y + 60);

        // Player coin window under Player inventory (right)
        x = playerInvFrameX;
        y = playerInvFrameY + playerInvFrameH + spacing;
        width = playerInvFrameW;
        height = gp.tileSize * 2;
        drawSubWindow(x, y, width, height);
        g2.drawString("Coins: " + gp.player.coin, x + 24, y + 60);

        // Draw Price window
        int itemIndex = getItemIndexOnslot(playerSlotCol, playerSlotRow);
        if (itemIndex != -1 && itemIndex < gp.player.inventory.size()) {
            x = (int) (gp.tileSize * 15.5);
            y = (int) (gp.tileSize * 5.5);
            width = (int) (gp.tileSize * 2.5);
            height = gp.tileSize;
            drawSubWindow(x, y, width, height);
            g2.drawImage(coin, x + 10, y + 10, 28, 28, null);

            int price = gp.player.inventory.get(itemIndex).price;
            String text = String.valueOf(price);
            x = getXforAlignToRightText(text, gp.tileSize * 18);
            g2.drawString(text, x - 19, y + 32);

            // Buy an item (Enter or F → enterPressed)
            if (gp.keyHandler.enterPressed) {

                if (gp.player.inventory.get(itemIndex) == gp.player.currentWeapon ||
                        gp.player.inventory.get(itemIndex) == gp.player.currentShield) {
                    // If the item is equipped - NPC dialogue set 1, index 2
                    commandNumber = 0;
                    subState = 0;
                    npc.startDialogue(npc, 1);
                    npc.dialogueIndex = 2;
                } else {
                    // Sell successful
                    gp.player.coin += gp.player.inventory.get(itemIndex).price;
                    gp.player.inventory.remove(itemIndex);
                }
                gp.keyHandler.enterPressed = false;
            }
        }

    }

    // Compute inventory frame rectangles for both NPC and Player without drawing.
    // Keeps alignment consistent even when we intentionally hide one panel.
    private void computeInventoryFrames() {
        final int padding = 20;
        int slotSize = gp.tileSize + 3;
        int maxFrameWidth = gp.screenWidth - (gp.tileSize * 2); // keep 1 tile margin both sides
        int neededWidth = padding + (inventoryCols * slotSize) + padding;
        if (neededWidth > maxFrameWidth) {
            int availableForSlots = Math.max(48, maxFrameWidth - (padding * 2));
            slotSize = Math.max(24, availableForSlots / inventoryCols);
            neededWidth = padding + (inventoryCols * slotSize) + padding;
        }
        int frameWidth = neededWidth;
        int frameHeight = padding + (inventoryRows * slotSize) + padding;

        // NPC frame (left, anchored from left)
        npcInvFrameW = frameWidth;
        npcInvFrameH = frameHeight;
        npcInvFrameX = gp.tileSize;
        npcInvFrameY = gp.tileSize;

        // Player frame (right, anchored from right)
        playerInvFrameW = frameWidth;
        playerInvFrameH = frameHeight;
        playerInvFrameX = Math.max(gp.tileSize, gp.screenWidth - frameWidth - gp.tileSize);
        playerInvFrameY = gp.tileSize;
    }

    public void drawTransition() {
        counter++;
        g2.setColor(new Color(0, 0, 0, counter * 5));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if (counter == 50) {
            counter = 0;
            gp.gameState = gp.playState;
            gp.currentMap = gp.eventHandler.tempMap;
            gp.player.worldX = gp.eventHandler.tempCol * gp.tileSize;
            gp.player.worldY = gp.eventHandler.tempRow * gp.tileSize;
            gp.eventHandler.previousEventX = gp.player.worldX;
            gp.eventHandler.previousEventY = gp.player.worldY;
            gp.changeArea();
        }
    }

    public void drawInventory(Entity entity, boolean cursor) {
        // Create inventory frame sized based on grid, ensure it fits the screen
        int frameWidth = 0, frameHeight = 0, frameX = 0, frameY = 0;
        int slotCol, slotRow;

        final int padding = 20;
        int slotSize = gp.tileSize + 3;
        int maxFrameWidth = gp.screenWidth - (gp.tileSize * 2); // keep 1 tile margin both sides
        // If too wide, shrink slot size to fit
        int neededWidth = padding + (inventoryCols * slotSize) + padding;
        if (neededWidth > maxFrameWidth) {
            int availableForSlots = Math.max(48, maxFrameWidth - (padding * 2));
            slotSize = Math.max(24, availableForSlots / inventoryCols);
            neededWidth = padding + (inventoryCols * slotSize) + padding;
        }
        if (entity == gp.player) {
            slotCol = playerSlotCol;
            slotRow = playerSlotRow;
            frameWidth = neededWidth;
            frameHeight = padding + (inventoryRows * slotSize) + padding;
            frameX = Math.max(gp.tileSize, gp.screenWidth - frameWidth - gp.tileSize); // anchor from right
            frameY = gp.tileSize;
            // Store for alignment
            playerInvFrameX = frameX;
            playerInvFrameY = frameY;
            playerInvFrameW = frameWidth;
            playerInvFrameH = frameHeight;
        } else {
            slotCol = npcSlotCol;
            slotRow = npcSlotRow;
            frameWidth = neededWidth;
            frameHeight = padding + (inventoryRows * slotSize) + padding;
            frameX = gp.tileSize; // anchor from left
            frameY = gp.tileSize;
            // Store for alignment
            npcInvFrameX = frameX;
            npcInvFrameY = frameY;
            npcInvFrameW = frameWidth;
            npcInvFrameH = frameHeight;
        }

        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // Slot grid origin and size
        final int slotXstart = frameX + padding;
        final int slotYstart = frameY + padding;
        int slotX = slotXstart;
        int slotY = slotYstart;

        // Draw entity's items
        for (int i = 0; i < entity.inventory.size(); i++) {

            // Equip cursor
            if (entity.inventory.get(i) == entity.currentWeapon
                    || entity.inventory.get(i) == entity.currentShield
                    || entity.inventory.get(i) == entity.currentLight) {
                // Draw equip cursor
                g2.setColor(new Color(240, 190, 90));
                g2.setStroke(new BasicStroke(3));
                g2.fillRoundRect(slotX, slotY, slotSize, slotSize, 10, 10);
            }
            // Draw item icon scaled to slot
            g2.drawImage(entity.inventory.get(i).down1, slotX, slotY, slotSize - 3, slotSize - 3, null);
            slotX += slotSize; // Move to the next slot
            // Move to next row after last column
            if ((i + 1) % inventoryCols == 0) {
                slotX = slotXstart; // Reset X
                slotY += slotSize; // Move down to the next row
            }
        }

        if (cursor) {
            // Cursor
            int cursorX = slotXstart + (slotSize * slotCol);
            int cursorY = slotYstart + (slotSize * slotRow);
            int cursorWidth = slotSize - 3;
            int cursorHeight = slotSize - 3;

            // Draw cursor
            g2.setColor(Color.white);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);

            // Description frame
            int dFrameX = frameX;
            int dFrameY = frameY + frameHeight;
            int dFrameWidth = frameWidth;
            int dFrameHeight = gp.tileSize * 3;

            // Draw description text
            int textX = dFrameX + 20;
            int textY = dFrameY + gp.tileSize;

            int itemIndex = getItemIndexOnslot(slotCol, slotRow);
            if (itemIndex < entity.inventory.size()) {
                drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);
                // drawSubWindow sets the font to Minecraft; now set a smaller font for the
                // description
                g2.setFont(Minecraft.deriveFont(20f));
                for (String line : entity.inventory.get(itemIndex).description.split("\n")) {
                    g2.drawString(line, textX, textY);
                    textY += 32;
                }
            }

        }
    }

    public int getItemIndexOnslot(int slotCol, int slotRow) {
        // Convert cursor position to inventory index based on grid columns
        int itemIndex = slotCol + (slotRow * inventoryCols);
        return itemIndex;
    }

    public void drawCharacterScreen() {
        // Create A Frame
        final int frameX = gp.tileSize;
        final int frameY = gp.tileSize;
        final int frameWidth = gp.tileSize * 5;
        final int frameHeight = gp.tileSize * 10 + 10;
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // Text
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(23f));

        int textX = frameX + 20;
        int textY = frameY + gp.tileSize;
        final int lineHeight = 36;

        // Names
        g2.drawString("Level", textX, textY);
        textY += lineHeight;
        g2.drawString("Life", textX, textY);
        textY += lineHeight;
        g2.drawString("Mana", textX, textY);
        textY += lineHeight;
        g2.drawString("Strength", textX, textY);
        textY += lineHeight;
        g2.drawString("Dexterity", textX, textY);
        textY += lineHeight;
        g2.drawString("Attack", textX, textY);
        textY += lineHeight;
        g2.drawString("Defense", textX, textY);
        textY += lineHeight;
        g2.drawString("Exp", textX, textY);
        textY += lineHeight;
        g2.drawString("Next Level", textX, textY);
        textY += lineHeight;
        g2.drawString("Coin", textX, textY);
        textY += lineHeight + 15;

        // Weapon
        g2.drawString("Weapon", textX, textY);
        textY += lineHeight + 10;
        g2.drawString("Shield", textX, textY);
        textY += lineHeight;

        // Values
        int tailX = frameX + frameWidth - 30;
        // Reset textY
        textY = frameY + gp.tileSize;
        String value;

        value = String.valueOf(gp.player.level);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Life
        textY += lineHeight;
        value = gp.player.life + "/" + gp.player.maxLife;
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Mana
        textY += lineHeight;
        value = gp.player.mana + "/" + gp.player.maxMana;
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Strength
        textY += lineHeight;
        value = String.valueOf(gp.player.strength);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Dexterity
        textY += lineHeight;
        value = String.valueOf(gp.player.dexterity);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Attack
        textY += lineHeight;
        value = String.valueOf(gp.player.attack);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Defense
        textY += lineHeight;
        value = String.valueOf(gp.player.defense);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Exp
        textY += lineHeight;
        value = String.valueOf(gp.player.exp);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Next Level
        textY += lineHeight;
        value = String.valueOf(gp.player.nextLevelExp);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        // Coin
        textY += lineHeight;
        value = String.valueOf(gp.player.coin);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);

        // Weapon icon (smaller)
        textY += lineHeight + 20;
        int iconSize = (int) (gp.tileSize * 0.75); // 75% of tile size
        int iconX = tailX - iconSize; // right-align icon to tail
        int iconY = textY - iconSize + Math.max(0, (lineHeight - iconSize) / 2);
        if (gp.player.currentWeapon != null && gp.player.currentWeapon.down1 != null) {
            g2.drawImage(gp.player.currentWeapon.down1, iconX + 6, iconY, iconSize, iconSize, null);
        }

        // Shield icon (smaller)
        textY += lineHeight + 13;
        iconY = textY - iconSize + Math.max(0, (lineHeight - iconSize) / 2);
        if (gp.player.currentShield != null && gp.player.currentShield.down1 != null) {
            g2.drawImage(gp.player.currentShield.down1, iconX + 11, iconY, iconSize, iconSize, null);
        }

    }

    public void drawOptionScreen() {
        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(15f));

        // SUB WINDOW
        int frameX = gp.tileSize * 6;
        int frameY = gp.tileSize;
        int frameWidth = gp.tileSize * 8;
        int frameHeight = gp.tileSize * 10;
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);
        // Title
        switch (subState) {
            case 0:
                option_top(frameX, frameY);
                break;
            case 1:
                options_fullScreenNotification(frameX, frameY);
                break;
            case 2:
                option_control(frameX, frameY);
                break;
            case 3:
                option_endGameConfirm(frameX, frameY);
                break;
        }

        gp.keyHandler.enterPressed = false;

    }

    public void option_top(int frameX, int frameY) {
        int textX;
        int textY;

        // Title
        String text = "Options";
        g2.setFont(Minecraft.deriveFont(28f));
        textX = getXforCenteredText(text);
        textY = frameY + gp.tileSize;
        g2.drawString(text, textX, textY);

        // Full screen ON/OFF
        textX = frameX + gp.tileSize;
        textY += gp.tileSize;
        g2.setFont(Minecraft.deriveFont(25f));
        g2.drawString("Full Screen", textX, textY);
        if (commandNumber == 0) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyHandler.enterPressed) {
                // Show notification/confirmation page first; apply on that page
                pendingFullScreenToggle = true;
                subState = 1;
                commandNumber = 0; // default focus on Apply
            }
        }

        // Music ON/OFF
        textY += gp.tileSize;
        g2.drawString("Music", textX, textY);
        if (commandNumber == 1) {
            g2.drawString(">", textX - 25, textY);
        }

        // SE
        textY += gp.tileSize;
        g2.drawString("Sound Effects", textX, textY);
        if (commandNumber == 2) {
            g2.drawString(">", textX - 25, textY);
        }

        // Control
        textY += gp.tileSize;
        g2.drawString("Controls", textX, textY);
        if (commandNumber == 3) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyHandler.enterPressed) {
                subState = 2;
                commandNumber = 0;
            }
        }

        // END game
        textY += gp.tileSize;
        g2.drawString("End Game", textX, textY);
        if (commandNumber == 4) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyHandler.enterPressed) {
                subState = 3;
                commandNumber = 0;
            }
        }

        // Back
        textY += gp.tileSize * 2;
        g2.drawString("Back", textX, textY);
        if (commandNumber == 5) {
            g2.drawString(">", textX - 25, textY);
            if (gp.keyHandler.enterPressed) {
                gp.gameState = gp.playState;
                commandNumber = 0;
            }
        }

        // Full screen check box
        textX = frameX + gp.tileSize * 4 + 50;
        textY = frameY + gp.tileSize * 2 - 20;
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(textX, textY, 24, 24);
        if (gp.fullscreenOn) {
            g2.fillRect(textX, textY, 24, 24);
        }

        // Music Volume
        textY += gp.tileSize;
        g2.drawRect(textX, textY, 120, 24);
        int volumeWidth = 24 * gp.music.volumeScale;
        g2.fillRect(textX, textY, volumeWidth, 24);

        // SE Volume
        textY += gp.tileSize;
        g2.drawRect(textX, textY, 120, 24);
        volumeWidth = 24 * gp.se.volumeScale;
        g2.fillRect(textX, textY, volumeWidth, 24);

    }

    public void drawPlayerHealth() {
        // Add a small top margin so HUD isn't tight to the top edge
        final int topHudMargin = gp.tileSize / 4; // ~12px at 48px tiles
        final int MAX_PER_ROW = 7; // Maximum hearts/crystals per row

        int startX = gp.tileSize / 2; // Start position for health display
        int x = startX;
        int y = gp.tileSize / 2 + topHudMargin; // Shifted down slightly
        int i = 0;

        // Draw blank hearts (max life)
        while (i < gp.player.maxLife / 2) {
            g2.drawImage(heart_blank, x, y, null);
            i++;
            x += gp.tileSize; // Move to the next heart position

            // Wrap to next row if exceeded 12 hearts
            if (i % MAX_PER_ROW == 0) {
                x = startX;
                y += gp.tileSize;
            }
        }

        // Reset position for current hearts
        x = startX;
        y = gp.tileSize / 2 + topHudMargin;
        i = 0;

        // Draw current hearts
        while (i < gp.player.life) {
            g2.drawImage(heart_half, x, y, null);
            i++;
            if (i < gp.player.life) {
                g2.drawImage(heart_full, x, y, null); // Draw full heart if life is odd
            }
            i += 1;
            x += gp.tileSize; // Move to the next heart position

            // Wrap to next row if exceeded 12 hearts
            if ((i / 2) % MAX_PER_ROW == 0) {
                x = startX;
                y += gp.tileSize;
            }
        }

        // Draw mana crystals (scaled up)
        int crystalSize = (int) (gp.tileSize * 1.15); // 120% of tile size
        int crystalGap = (int) (crystalSize * 0.65); // spacing between crystals
        int crystalStartX = gp.tileSize / 2 - 9;

        // Calculate y position: hearts can take multiple rows, so start after all heart
        // rows
        int heartRows = (gp.player.maxLife / 2 + MAX_PER_ROW - 1) / MAX_PER_ROW; // Ceiling division
        int manaStartY = (gp.tileSize / 2 + topHudMargin) + (heartRows * gp.tileSize);

        x = crystalStartX;
        y = manaStartY;
        i = 0;

        // Draw blank crystals for max mana
        while (i < gp.player.maxMana) {
            g2.drawImage(crystal_blank, x, y, crystalSize, crystalSize, null);
            i++;
            x += crystalGap; // Move to the next crystal position

            // Wrap to next row if exceeded 12 crystals
            if (i % MAX_PER_ROW == 0) {
                x = crystalStartX;
                y += crystalSize;
            }
        }

        // Reset position for current mana
        x = crystalStartX;
        y = manaStartY;
        i = 0;

        // Draw full crystals for current mana
        while (i < gp.player.mana) {
            g2.drawImage(crystal_full, x, y, crystalSize, crystalSize, null);
            i++;
            x += crystalGap; // Move to the next crystal position

            // Wrap to next row if exceeded 12 crystals
            if (i % MAX_PER_ROW == 0) {
                x = crystalStartX;
                y += crystalSize;
            }
        }
    }

    public void options_fullScreenNotification(int frameX, int frameY) {
        int textX = frameX + gp.tileSize;
        int textY = frameY + gp.tileSize * 3;

        // Title line larger
        g2.setFont(Minecraft.deriveFont(30f));
        g2.drawString("Toggle Full Screen?", textX, textY);
        // Body line slightly smaller
        textY += 48;
        g2.setFont(Minecraft.deriveFont(24f));
        g2.drawString("This may resize the window.", textX, textY);

        // Apply and Back buttons centered near bottom
        g2.setFont(Minecraft.deriveFont(28f));
        String applyText = "Apply";
        String backText = "Back";
        int applyY = frameY + gp.tileSize * 8;
        int backY = applyY + gp.tileSize;
        int applyX = getXforCenteredText(applyText);
        int backX = getXforCenteredText(backText);

        g2.drawString(applyText, applyX, applyY);
        g2.drawString(backText, backX, backY);

        if (commandNumber == 0) {
            g2.drawString(">", applyX - 30, applyY);
            if (gp.keyHandler.enterPressed) {
                // Apply pending toggle if any
                if (pendingFullScreenToggle) {
                    if (!gp.fullscreenOn) {
                        gp.fullscreenOn = true;
                        gp.setFullScreen();
                    } else {
                        gp.fullscreenOn = false;
                        gp.setWindowed(1280, 720);
                    }
                    // Persist to config after applying
                    gp.config.saveConfig();
                    pendingFullScreenToggle = false;
                }
                subState = 0;
                commandNumber = 0; // return focus to Full Screen item
            }
        } else if (commandNumber == 1) {
            g2.drawString(">", backX - 30, backY);
            if (gp.keyHandler.enterPressed) {
                // Cancel and go back without changing
                pendingFullScreenToggle = false;
                subState = 0;
                commandNumber = 0; // return focus to Full Screen item
            }
        }
    }

    public void drawSleepScreen() {

        counter++;

        if (counter < 1000) {
            gp.envManager.lighting.filterAlpha += 0.01f;
            if (gp.envManager.lighting.filterAlpha > 1f) {
                gp.envManager.lighting.filterAlpha = 1f;
            }
        }

        if (counter >= 1000) {
            gp.envManager.lighting.filterAlpha -= 0.01f;
            if (gp.envManager.lighting.filterAlpha <= 0f) {
                gp.envManager.lighting.filterAlpha = 0f;
                counter = 0;
                gp.envManager.lighting.dayState = gp.envManager.lighting.day;
                gp.gameState = gp.playState;
            }
        }

    }

    public void option_endGameConfirm(int frameX, int frameY) {
        int textX = frameX + gp.tileSize;
        int textY = frameY + gp.tileSize;
        // Match font size with Controls page
        g2.setFont(Minecraft.deriveFont(25f));

        currentDialogue = "Are you sure you want to \nend the game and return \nto the title screen?";
        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40; // line spacing
        }

        // Yes
        g2.setFont(Minecraft.deriveFont(23f));
        String text = "Yes";
        textX = getXforCenteredText(text);
        textY += gp.tileSize * 3;
        g2.drawString(text, textX, textY);
        if (commandNumber == 0) {
            g2.drawString(">", textX - 30, textY);
            if (gp.keyHandler.enterPressed) {
                // Stop any playing background music when returning to title
                gp.stopMusic();
                gp.resetGame(true);
                subState = 0;
                gp.gameState = gp.titleState;
                // Reset title menu selection to NEW GAME
                commandNumber = 0;
            }
        }

        // No
        text = "No";
        textX = getXforCenteredText(text);
        textY += gp.tileSize;
        g2.drawString(text, textX, textY);
        if (commandNumber == 1) {
            g2.drawString(">", textX - 30, textY);
            if (gp.keyHandler.enterPressed) {
                subState = 0;
                commandNumber = 4; // Back to "End Game" option
            }
        }
    }

    public void drawPauseScreen() {
        // Don't draw any background overlay - just show pause text directly

        // Draw pause text
        String pauseText = "GAME PAUSED";
        g2.setFont(Minecraft);
        g2.setColor(Color.WHITE);
        int x = getXforCenteredText(pauseText);
        int y = gp.screenHeight / 2 - 50;
        g2.drawString(pauseText, x, y);

        // Draw resume instruction
        g2.setFont(Minecraft);
        String resumeInstruction = "Press P to Resume";
        x = getXforCenteredText(resumeInstruction);
        y = gp.screenHeight / 2 + 50;
        g2.setColor(Color.WHITE);
        g2.drawString(resumeInstruction, x, y);
    }

    public void drawMessages() {
        int messageX = gp.tileSize / 2;
        int messageY = gp.tileSize * 6; // Start drawing messages below health display
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 25)); // Set font size for messages

        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i) != null) {

                g2.setColor(Color.black);
                g2.drawString(messages.get(i), messageX + 2, messageY + 2);
                g2.setColor(Color.WHITE);
                g2.drawString(messages.get(i), messageX, messageY); // Stack messages vertically

                int counter = messageCounters.get(i);
                messageCounters.set(i, counter);
                messageY += 30;

                // Remove the message if it has been displayed for 120 frames (~2 seconds at 60
                // FPS)
                if (messageCounters.get(i) > 120) {
                    messages.remove(i);
                    messageCounters.remove(i);
                    i--; // Adjust index after removal
                } else {
                    messageCounters.set(i, counter + 1); // Increment the counter
                }
            }
        }

    }

    public void drawTitleScreen() {

        g2.setColor(new Color(0, 0, 0)); // Set background color
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw title screen background
        g2.setFont(g2.getFont().deriveFont(60f)); // Set larger font for title
        String titleText = "Aiden Adventure Game"; // Replace with your game title
        int x = getXforCenteredText(titleText);
        int y = gp.screenHeight / 3; // Fixed: divide by 3, not multiply by 3

        // Shadow
        g2.setColor(Color.GRAY);
        g2.drawString(titleText, x + 5, y - 65);
        g2.setColor(Color.WHITE);
        g2.drawString(titleText, x, y - 70);

        // Player image with idle animation
        x = gp.screenWidth / 2 - (gp.tileSize * 5) / 2; // Center the image
        y = gp.screenHeight / 2 - gp.tileSize * 5; // Center the image

        // Update animation for title screen
        titleAnimationCounter++;
        if (titleAnimationCounter > titleAnimationSpeed) {
            titleCurrentFrame++;
            if (titleCurrentFrame >= 6) { // Idle animation has 6 frames
                titleCurrentFrame = 0;
            }
            titleAnimationCounter = 0;
        }

        // Use the player's idle animation images
        if (gp.player.animationImages != null && gp.player.animationImages[4] != null &&
                titleCurrentFrame < gp.player.animationImages[4].length &&
                gp.player.animationImages[4][titleCurrentFrame] != null) {
            g2.drawImage(gp.player.animationImages[4][titleCurrentFrame], x, y, gp.tileSize * 5, gp.tileSize * 5, null);
        }

        // MENU
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30f)); // Set font size for menu

        // NEW GAME
        String menuText = "NEW GAME";
        int menuX = getXforCenteredText(menuText);
        int menuY = gp.screenHeight / 4 + 190; // Adjusted for better visibility
        g2.setColor(Color.WHITE);
        g2.drawString(menuText, menuX, menuY);
        if (commandNumber == 0) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", menuX - 50, menuY);
        }

    // QUIT GAME
        menuText = "QUIT GAME";
        menuX = getXforCenteredText(menuText);
        g2.setColor(Color.WHITE);
        g2.drawString(menuText, menuX, menuY + 70); // Adjusted position for better visibility
        if (commandNumber == 1) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", menuX - 50, menuY + 70);
        }

    }

    public void drawDialogueScreen() {
        // Window
        int x = gp.tileSize * 2;
        int y = gp.tileSize;
        int width = gp.screenWidth - gp.tileSize * 6;
        int height = gp.tileSize * 4;
        drawSubWindow(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(28f)); // Set font size for dialogue text
        x += gp.tileSize;
        y += gp.tileSize;

        if (npc.dialogues[npc.dialogueSet][npc.dialogueIndex] != null) {

            // currentDialogue = npc.dialogues[npc.dialogueSet][npc.dialogueIndex];

            char character[] = npc.dialogues[npc.dialogueSet][npc.dialogueIndex].toCharArray();

            if (charIndex < character.length) {
                typewriterCounter++;
                if (typewriterCounter > 1) { // Delay 3 frames between each character (slower)
                    gp.playSoundEffect(9);
                    String s = String.valueOf(character[charIndex]);
                    combinedText += s;
                    currentDialogue = combinedText;
                    charIndex++;
                    typewriterCounter = 0;
                }
            }

            if (gp.keyHandler.fPressed) {

                charIndex = 0;
                combinedText = "";

                if (gp.gameState == gp.dialogueState || gp.gameState == gp.cutsceneState) {
                    npc.dialogueIndex++;
                    gp.keyHandler.fPressed = false;
                }
            }
        } else {
            // Dialogue finished for current set
            // Reset typewriter state
            charIndex = 0;
            combinedText = "";
            currentDialogue = "";
            typewriterCounter = 0;

            // Reset dialogue index for next time
            npc.dialogueIndex = 0;

            if (gp.gameState == gp.dialogueState) {
                // Close dialogue and return to play
                gp.gameState = gp.playState;
                // Clear active dialogue speaker
                npc = null;
            } else if (gp.gameState == gp.cutsceneState) {
                // Prevent dialogue from restarting in cutscene by clearing npc
                npc = null;
                // Advance cutscene phase
                gp.cutsceneManager.scenePhase++;
            }
        }

        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, x, y);
            y += 30; // Move down for next line
        }
    }

    public void drawSubWindow(int x, int y, int width, int height) {
        // Draw a semi-transparent background for the sub-window
        g2.setColor(new Color(0, 0, 0, 150)); // Black with 150 alpha for transparency
        g2.fillRoundRect(x, y, width, height, 35, 35);

        // Draw the border
        g2.setColor(Color.WHITE);
        // Do not change the caller's font here; caller will set appropriate font size
        g2.setStroke(new BasicStroke(5)); // Thicker stroke for title
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);

    }

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2; // Calculate x position for centered text
    }

    public int getXforAlignToRightText(String text, int tailX) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return tailX - length; // Calculate x position for right-aligned text
    }

    public void option_control(int frameX, int frameY) {
        // Layout
        final int leftX = frameX + gp.tileSize;
        final int rightTailX = frameX + gp.tileSize * 8 - 30; // align keys to inner right edge
        int y = frameY + gp.tileSize * 2;
        final int line = gp.tileSize; // vertical spacing

        // Fonts
        g2.setFont(Minecraft.deriveFont(22f));
        g2.setColor(Color.WHITE);

        // Labels and corresponding keys
        String[] labels = {
                "Move", "Confirm/Attack", "Cast", "Shoot", "Character Screen", "Pause", "Options"
        };
        String[] keys = {
                "WASD", "ENTER", "F", "E", "C", "P", "ESC"
        };

        for (int i = 0; i < labels.length; i++) {
            g2.drawString(labels[i], leftX, y);
            int keyX = getXforAlignToRightText(keys[i], rightTailX);
            g2.drawString(keys[i], keyX, y);
            y += line;
        }

        // BACK
        int backX = leftX;
        int backY = frameY + gp.tileSize * 9;
        g2.drawString("Back", backX, backY);
        if (commandNumber == 0) {
            g2.drawString(">", backX - 20, backY);
            if (gp.keyHandler.enterPressed) {
                subState = 0;
                commandNumber = 3; // Back to "Controls" option
            }
        }
    }

    public void drawGameOverScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Title
        String text = "GAME OVER";
        g2.setFont(Minecraft.deriveFont(90f));
        int x = getXforCenteredText(text);
        int y = gp.screenHeight / 2 - gp.tileSize; // on-screen center-ish
        // Shadow + main
        g2.setColor(Color.black);
        g2.drawString(text, x + 4, y + 4);
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        // Options below
        g2.setFont(Minecraft.deriveFont(40f));
        int optionY = y + gp.tileSize * 2;
        text = "RETRY";
        x = getXforCenteredText(text);
        g2.drawString(text, x, optionY);
        if (commandNumber == 0) {
            g2.drawString(">", x - 40, optionY);
        }

        text = "Quit";
        x = getXforCenteredText(text);
        optionY += 50;
        g2.drawString(text, x, optionY + gp.tileSize);
        if (commandNumber == 1) {
            g2.drawString(">", x - 40, optionY + gp.tileSize);
        }
    }
}
