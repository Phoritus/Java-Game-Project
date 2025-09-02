package src.main;
import javax.swing.JFrame;

public class Main {
    
    public static JFrame window;
    public static void main(String[] args) {
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setResizable(true);
        window.setTitle("2D Adventure Game");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

    // Set up the game (GamePanel will choose windowed or fullscreen)
    gamePanel.setupGame();

    // Now show the window (GamePanel.setupGame may already make it visible)
    if (!window.isVisible()) {
        window.setVisible(true);
    }

    gamePanel.startGameThread(); // Start the game loop
    }
}