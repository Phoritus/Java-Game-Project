package src.object;

import java.awt.image.BufferedImage;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Key extends Entity {

    private OBJ_Animation animation;

    public OBJ_Key(GamePanel gp) {
        super(gp);
        name = "Key";
        price = 5;

        // Load animation frames for the key spin
        BufferedImage[] frames = new BufferedImage[4];
        frames[0] = setup("/res/objects/key/keys_1_1.png");
        frames[1] = setup("/res/objects/key/keys_1_2.png");
        frames[2] = setup("/res/objects/key/keys_1_3.png");
        frames[3] = setup("/res/objects/key/keys_1_4.png");

        // Create looping animation (slightly slow spin)
        animation = new OBJ_Animation(frames, 12);
        down1 = frames[0]; // initial frame for fallback draw

        description = "[" + name + "]\nA small key that\ncan open locked doors.";
    // Keys should not block movement
    this.collision = false;
    // Generous pickup area centered in the tile
    this.solidArea = new java.awt.Rectangle(8, 8, gp.tileSize - 16, gp.tileSize - 16);
    this.solidAreaDefaultX = this.solidArea.x;
    this.solidAreaDefaultY = this.solidArea.y;
    }

    @Override
    public void update() {
        if (animation != null) {
            animation.update();
            BufferedImage cur = animation.getCurrentFrame();
            if (cur != null) {
                down1 = cur; // Let base draw() use the updated frame
            }
        }
    }
}
