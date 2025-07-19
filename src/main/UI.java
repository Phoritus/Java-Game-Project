package src.main;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

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
    public String message = ""; // Message to display on the UI
    int messageCounter = 0; // Counter for message display duration
    public boolean gameFinished = false; // Flag to indicate if the game is finished
    public String currentDialogue = ""; // Current dialogue text to display
    public int commandNumber = 0; // Command number for dialogue or actions

    // Animation variables for title screen
    public int titleAnimationCounter = 0;
    public int titleCurrentFrame = 0;
    public int titleAnimationSpeed = 1; // Slower animation for title screen

    BufferedImage heart_full, heart_half, heart_blank; // Heart images for health display

    public UI(GamePanel gp) {
        this.gp = gp; // Initialize the GamePanel reference
        try {
            InputStream is = getClass().getResourceAsStream("/res/font/Minecraft.ttf");
            Minecraft = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(40f); // Load Minecraft font
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions, such as file not found
        }
        //  Create heart images for health display
        Entity heart = new OBJ_Heart(gp);
        heart_full = heart.image; // Full heart image
        heart_half = heart.image2; // Half heart image
        heart_blank = heart.image3; // Blank heart image
    }

    public void showMessage(String text) {
        message = text; // Set the message to be displayed
        messageOn = true; // Enable message display
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

    }

    public void drawPlayerHealth() {
        
        //gp.player.life = 3;

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
            i+=1;
            x += gp.tileSize; // Move to the next heart position
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

    public void drawTitleScreen() {

        g2.setColor(new Color(0,0,0)); // Set background color
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
}
