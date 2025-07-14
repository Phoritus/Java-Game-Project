package src.main;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

public class UI {

    GamePanel gp; // Reference to the GamePanel for accessing game settings
    Font arial_40, arial_40U, arial_80; // Fonts for displaying text
    Graphics2D g2; // Graphics context for drawing
    public boolean messageOn = false; // Flag to control message display
    public String message = ""; // Message to display on the UI
    int messageCounter = 0; // Counter for message display duration
    public boolean gameFinished = false; // Flag to indicate if the game is finished

    double gameTime = 0; // Variable to track game time

    public UI(GamePanel gp) {
        this.gp = gp; // Initialize the GamePanel reference
        arial_40 = new Font("Arial", Font.PLAIN, 40);
        arial_40U = new Font("Arial", Font.BOLD, 40);
        arial_80 = new Font("Arial", Font.BOLD, 80); // Initialize the font for large text
//      OBJ_Key key = new OBJ_Key(gp); // Create an instance of OBJ_Key to access its image
//      keyImage = key.image; // Get the key image from the OBJ_Key instance
    }

    public void showMessage(String text) {
        message = text; // Set the message to be displayed
        messageOn = true; // Enable message display
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;

        g2.setFont(arial_40);
        g2.setColor(Color.white); // Set text color to white
        
        if (gp.gameState == gp.playState) {
            // Draw game time
            g2.drawString("Time: " + String.format("%.2f", gameTime), 10, 40);
            
            // Draw music toggle button at top right
            drawMusicButton();
        } else if (gp.gameState == gp.pauseState) {
            drawPauseScreen();
            
            // Draw music button even when paused
            drawMusicButton();
        }

    }


    public void drawPauseScreen() {
        // Don't draw any background overlay - just show pause text directly
        
        // Draw pause text
        String pauseText = "GAME PAUSED";
        g2.setFont(arial_80);
        g2.setColor(Color.WHITE);
        int x = getXforCenteredText(pauseText);
        int y = gp.screenHeight / 2 - 50;
        g2.drawString(pauseText, x, y);
        
        // Draw resume instruction
        g2.setFont(arial_40);
        String resumeInstruction = "Press P to Resume";
        x = getXforCenteredText(resumeInstruction);
        y = gp.screenHeight / 2 + 50;
        g2.setColor(Color.WHITE);
        g2.drawString(resumeInstruction, x, y);
    }
    
    public void drawMusicButton() {
        // Draw music toggle button at top right corner
        String musicText = gp.musicOn ? "♪ ON" : "♪ OFF";
        g2.setFont(arial_40);
        
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

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth / 2 - length / 2; // Calculate x position for centered text
    }
}
