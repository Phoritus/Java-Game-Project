package src.tile;

import java.awt.image.BufferedImage;

public class Tile {

    public BufferedImage image; // Image for the tile
    public boolean collision = false; // Collision flag for the tile
    
    // Animation properties
    public BufferedImage[] animationFrames; // Array for animation frames
    public boolean hasAnimation = false; // Flag to check if tile has animation
    public int animationFrame = 0; // Current animation frame
    public int animationCounter = 0; // Counter for animation timing
    public int animationSpeed = 20; // How fast animation plays (higher = slower)
    
    // Constructor for static tiles
    public Tile() {
        this.hasAnimation = false;
    }
    
    // Constructor for animated tiles
    public Tile(BufferedImage[] frames, int animationSpeed) {
        this.animationFrames = frames;
        this.hasAnimation = true;
        this.animationSpeed = animationSpeed;
        if (frames != null && frames.length > 0) {
            this.image = frames[0]; // Set first frame as default image
        }
    }
    
    // Method to update animation
    public void updateAnimation() {
        if (hasAnimation && animationFrames != null && animationFrames.length > 0) {
            animationCounter++;
            if (animationCounter > animationSpeed) {
                animationCounter = 0;
                animationFrame++;
                if (animationFrame >= animationFrames.length) {
                    animationFrame = 0;
                }
                image = animationFrames[animationFrame];
            }
        }
    }
    
    // Method to get current animation frame
    public BufferedImage getCurrentFrame() {
        if (hasAnimation && animationFrames != null && animationFrames.length > 0) {
            return animationFrames[animationFrame];
        }
        return image;
    }
}
