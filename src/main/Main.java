package src.main;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Main {

    public static JFrame window;

    public static void main(String[] args) {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Aiden Adventure Game");
        new Main().setIcon();

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        gamePanel.config.loadConfig(); // Load config settings before setting up the game
        if (gamePanel.fullscreenOn) {
            window.setUndecorated(true);
        }

        // Set up the game (GamePanel will choose windowed or fullscreen)
        gamePanel.setupGame();

        // Now show the window (GamePanel.setupGame may already make it visible)
        if (!window.isVisible()) {
            window.setVisible(true);
        }

        gamePanel.startGameThread(); // Start the game loop
    }

    public void setIcon() {
        // Set the game window icon
        ImageIcon icon = new ImageIcon(getClass().getResource("/res/objects/icon_game.png"));
        window.setIconImage(icon.getImage());
    }
}