package src.entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entity {
    public int worldX, worldY; // World coordinates of the entity
    public int screenX, screenY; // Screen coordinates of the entity
    public int speed; // Speed of the entity

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2; // Images for animations
    public String direction; // Direction of the entity

    public int spriteCounter = 0; // Counter for animation frames
    public int spriteNum = 1; // Current sprite number for animation

    public Rectangle solidArea; // Rectangle for collision detection
    public boolean collisionOn = false; // Flag for collision detection

    public int solidAreaDefaultX, solidAreaDefaultY; // Default position of the solid area
}
