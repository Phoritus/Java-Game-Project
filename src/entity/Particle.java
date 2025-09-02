package src.entity;

import java.awt.Color;
import java.awt.Graphics2D;

import src.main.GamePanel;

public class Particle extends Entity {
    Entity generator; // The entity that generated this particle
    Color color; // The color of the particle
    int xd; int yd; int size;
    GamePanel gp;
    // Use a dedicated speed for particles so we don't interfere with Entity.speed
    int pSpeed;

    public Particle(GamePanel gp, Color color, Entity generator, int size, int maxLife, int speed, int xd, int yd) {
        super(gp);
        this.color = color;
        this.generator = generator;
        this.size = size;
        this.maxLife = maxLife;
        this.pSpeed = speed;
        this.xd = xd;
        this.yd = yd;

        life = maxLife;
        int offset = (gp.tileSize / 2) - (size / 2);
        worldX = generator.worldX + offset;
        worldY = generator.worldY + offset;
    }

    public void update() {
        worldX += xd * pSpeed;
        worldY += yd * pSpeed;
        life--;
    // Start gravity in the second half so chips eject sideways then fall
    if (life < maxLife / 3) {
            yd++;
        }
        // Despawn when life runs out
        if (life <= -1) {
            alive = false;
        }
    }

    // Ensure GamePanel's entity renderer calls this version
    public void draw(Graphics2D g2, GamePanel gp) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;
        g2.setColor(color);
        g2.fillRect(screenX, screenY, size, size);
    }
}
