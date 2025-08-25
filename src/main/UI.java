package src.main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

import src.entity.Entity;
import src.object.OBJ_Heart;

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
    public int slotCol = 0;
    public int slotRow = 0;

    BufferedImage heart_full, heart_half, heart_blank; // Heart images for health display

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

        if (gp.gameState == gp.playState) {
            // Draw music toggle button at top right
            drawPlayerHealth();
            drawMessages();
            drawMusicButton();
        } else if (gp.gameState == gp.pauseState) {
            drawPlayerHealth();
            drawPauseScreen();

            // Draw music button even when paused
            drawMusicButton();
        }

        // Dialog state
        if (gp.gameState == gp.dialogState) {
            drawPlayerHealth();
            drawDialogScreen();
        }

        // Character customization state
        if (gp.gameState == gp.characterState) {
            drawCharacterScreen();
            drawInventory();
        }

    }

    public void drawInventory() {
        // Create A Frame
        final int frameX = gp.tileSize * 9;
        final int frameY = gp.tileSize;
        final int frameWidth = gp.tileSize * 6;
        final int frameHeight = gp.tileSize * 5;
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        // Slot
        final int slotXstart = frameX + 20;
        final int slotYstart = frameY + 20;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = gp.tileSize + 3;

        // Draw player's items
        for (int i = 0; i < gp.player.inventory.size(); i++) {

            // Equip cursor
            if (gp.player.inventory.get(i) == gp.player.currentWeapon 
            || gp.player.inventory.get(i) == gp.player.currentShield) {
                // Draw equip cursor
                g2.setColor(new Color(240, 190, 90));
                g2.setStroke(new BasicStroke(3));
                g2.fillRoundRect(slotX, slotY, gp.tileSize, gp.tileSize, 10, 10);
            }
            g2.drawImage(gp.player.inventory.get(i).down1, slotX, slotY, null);
            slotX += slotSize; // Move to the next slot
            if (i % 4 == 3) { // If 4 items are in a row
                slotX = slotXstart; // Reset X
                slotY += slotSize; // Move down to the next row
            }
        }

        // Cursor
        int cursorX = slotXstart + (slotSize * slotCol);
        int cursorY = slotYstart + (slotSize * slotRow);
        int cursorWidth = gp.tileSize;
        int cursorHeight = gp.tileSize;

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

        int itemIndex = getItemIndexOnslot();
        if (itemIndex < gp.player.inventory.size()) {
            drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);
            // drawSubWindow sets the font to Minecraft; now set a smaller font for the description
            g2.setFont(Minecraft.deriveFont(20f));
            for (String line : gp.player.inventory.get(itemIndex).description.split("\n")) {
                g2.drawString(line, textX, textY);
                textY += 32;
            }
        }

    }

    public int getItemIndexOnslot() {
        // Inventory grid is 4 columns per row in drawInventory
        int itemIndex = slotCol + (slotRow * 4);
        return itemIndex;
    }

    public void drawCharacterScreen() {
        // Create A Frame
        final int frameX = gp.tileSize;
        final int frameY = gp.tileSize;
        final int frameWidth = gp.tileSize * 5;
        final int frameHeight = gp.tileSize * 10;
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

    public void drawPlayerHealth() {

        // gp.player.life = 3;

        int x = gp.tileSize / 2; // Start position for health display
        int y = gp.tileSize / 2; // Start position for health display
        int i = 0;

        // Draw full hearts
        while (i < gp.player.life / 2) {
            g2.drawImage(heart_blank, x, y, null);
            x += gp.tileSize; // Move to the next heart position
            i++;
        }

        // Reset x position for full hearts
        x = gp.tileSize / 2;
        y = gp.tileSize / 2;
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
        }

    // Keys are displayed in inventory only (no HUD counter)

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

    public void drawMusicButton() {
        // Draw music toggle button at top right corner
        String musicText = gp.musicOn ? "MUSIC ON" : "MUSIC OFF";
        g2.setFont(Minecraft);

        // Set color based on music state
        if (gp.musicOn) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(Color.GRAY);
        }

        // Position at top right
        int textWidth = (int) g2.getFontMetrics().getStringBounds(musicText, g2).getWidth();
        int x = gp.screenWidth - textWidth - 20; // 20 pixels from right edge
        int y = 40; // Same height as time display

        g2.drawString(musicText, x, y);

        // Draw instruction text
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String instructionText = "Press M to toggle";
        int instructionWidth = (int) g2.getFontMetrics().getStringBounds(instructionText, g2).getWidth();
        int instructionX = gp.screenWidth - instructionWidth - 10;
        int instructionY = 65;
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(instructionText, instructionX, instructionY);
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
        String titleText = "Nigga Adventure Game"; // Replace with your game title
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

        // LOAD GAME
        menuText = "LOAD GAME";
        menuX = getXforCenteredText(menuText);
        g2.setColor(Color.WHITE);
        g2.drawString(menuText, menuX, menuY + 70); // Adjusted position for better visibility
        if (commandNumber == 1) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", menuX - 50, menuY + 70);
        }

        // QUIT GAME
        menuText = "QUIT GAME";
        menuX = getXforCenteredText(menuText);
        g2.setColor(Color.WHITE);
        g2.drawString(menuText, menuX, menuY + 140); // Adjusted position for better visibility
        if (commandNumber == 2) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", menuX - 50, menuY + 140);
        }

    }

    public void drawDialogScreen() {
        // Window
        int x = gp.tileSize * 2;
        int y = gp.tileSize / 2;
        int width = gp.screenWidth - gp.tileSize * 4;
        int height = gp.tileSize * 4;
        drawSubWindow(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(28f)); // Set font size for dialogue text
        x += gp.tileSize;
        y += gp.tileSize;

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

        // Draw title text
        g2.setFont(Minecraft);
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
}
