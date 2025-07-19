package src.object;

import java.awt.image.BufferedImage;

public class OBJ_Animation {
    // Animation frames
    private BufferedImage[] frames;
    private BufferedImage[] alternateFrames; // For chest opening animation
    private int currentFrame;
    private int frameCounter; // Count frames instead of time-based
    private int frameSpeed; // Animation speed (higher = slower)
    private boolean useAlternateFrames; // Switch to alternate frames
    private boolean isAnimating; // Control animation state
    private boolean loop; // Whether to loop animation
    private boolean animationComplete; // Whether animation is complete

    // Constructor for simple animation (like boot)
    public OBJ_Animation(BufferedImage[] frames, int frameSpeed) {
        this.frames = frames;
        this.frameSpeed = frameSpeed;
        this.currentFrame = 0;
        this.frameCounter = 0;
        this.isAnimating = true;
        this.loop = true;
        this.useAlternateFrames = false;
        this.animationComplete = false;
    }
    
    // Constructor for animation with 2 frame sets (like chest)
    public OBJ_Animation(BufferedImage[] mainFrames, BufferedImage[] alternateFrames, int frameSpeed) {
        this.frames = mainFrames;
        this.alternateFrames = alternateFrames;
        this.frameSpeed = frameSpeed;
        this.currentFrame = 0;
        this.frameCounter = 0;
        this.isAnimating = true;
        this.loop = true;
        this.useAlternateFrames = false;
        this.animationComplete = false;
    }

    public void update() {
        if (!isAnimating) return;
        
        frameCounter++;
        if (frameCounter > frameSpeed) {
            frameCounter = 0;
            
            BufferedImage[] activeFrames = useAlternateFrames ? alternateFrames : frames;
            if (activeFrames != null) {
                currentFrame++;
                
                if (currentFrame >= activeFrames.length) {
                    if (loop) {
                        currentFrame = 0; // Loop back to start
                    } else {
                        currentFrame = activeFrames.length - 1; // Stay on last frame
                        animationComplete = true;
                        isAnimating = false;
                    }
                }
            }
        }
    }
    
    // Start opening animation for chest
    public void startOpeningAnimation() {
        if (alternateFrames != null) {
            useAlternateFrames = true;
            currentFrame = 0;
            frameCounter = 0;
            loop = false; // Opening animation doesn't loop
            isAnimating = true;
            animationComplete = false;
        }
    }
    
    // Switch to alternate frames
    public void switchToAlternateFrames() {
        if (alternateFrames != null) {
            useAlternateFrames = true;
            currentFrame = 0;
            frameCounter = 0;
        }
    }
    
    // Switch back to main frames
    public void switchToMainFrames() {
        useAlternateFrames = false;
        currentFrame = 0;
        frameCounter = 0;
        loop = true;
        isAnimating = true;
        animationComplete = false;
    }
    
    // Pause/resume animation
    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
    }
    
    // Set animation loop
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    
    // Reset animation
    public void reset() {
        currentFrame = 0;
        frameCounter = 0;
        animationComplete = false;
        isAnimating = true;
    }
    
    // Change animation speed
    public void setFrameSpeed(int frameSpeed) {
        this.frameSpeed = frameSpeed;
    }

    public BufferedImage getCurrentFrame() {
        BufferedImage[] activeFrames = useAlternateFrames ? alternateFrames : frames;
        if (activeFrames != null && currentFrame < activeFrames.length) {
            return activeFrames[currentFrame];
        }
        return frames != null && frames.length > 0 ? frames[0] : null;
    }
    
    // Check if animation is complete
    public boolean isAnimationComplete() {
        return animationComplete;
    }
    
    // Check if using alternate frames
    public boolean isUsingAlternateFrames() {
        return useAlternateFrames;
    }
    
    // Get current frame index
    public int getCurrentFrameIndex() {
        return currentFrame;
    }
    
    // Get total number of frames
    public int getTotalFrames() {
        BufferedImage[] activeFrames = useAlternateFrames ? alternateFrames : frames;
        return activeFrames != null ? activeFrames.length : 0;
    }
}
