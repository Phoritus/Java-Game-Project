package src.main;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("My Application");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("2D Adventure Game");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack(); // Adjusts the window size to fit the GamePanel

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.setupGame(); // Set up the game (assets, player, etc.)
        gamePanel.startGameThread(); // Start the game loop
    }
}