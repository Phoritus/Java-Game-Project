package src.object;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Coin extends Entity {

    private OBJ_Animation animation;

    public OBJ_Coin(GamePanel gp) {
        super(gp);
        this.gp = gp;

        type = TYPE_PICKUP_ONLY;
        name = "Normal Coin";
        value = 1;

        // Coins should not block movement; make pickup area generous
        this.collision = false;
        this.solidArea = new java.awt.Rectangle(8, 8, gp.tileSize - 16, gp.tileSize - 16);
        this.solidAreaDefaultX = this.solidArea.x;
        this.solidAreaDefaultY = this.solidArea.y;

        // Load spin frames from common coin sprite names
    BufferedImage[] frames = new BufferedImage[9];
        frames[0] = trySetup(
            "/res/objects/coin/coin0.png",
            "/res/objects/coin/coin1.png",
            "/res/objects/coin/01.png"
        );
        frames[1] = trySetup(
            "/res/objects/coin/coin1.png",
            "/res/objects/coin/coin2.png",
            "/res/objects/coin/02.png"
        );
        frames[2] = trySetup(
            "/res/objects/coin/coin2.png",
            "/res/objects/coin/coin3.png",
            "/res/objects/coin/03.png"
        );
        frames[3] = trySetup(
            "/res/objects/coin/coin3.png",
            "/res/objects/coin/coin4.png",
            "/res/objects/coin/04.png"
        );
        frames[4] = trySetup(
            "/res/objects/coin/coin4.png",
            "/res/objects/coin/coin5.png",
            "/res/objects/coin/05.png"
        );
        frames[5] = trySetup(
            "/res/objects/coin/coin5.png",
            "/res/objects/coin/coin6.png",
            "/res/objects/coin/06.png"
        );
        frames[6] = trySetup(
            "/res/objects/coin/coin6.png",
            "/res/objects/coin/coin6.png",
            "/res/objects/coin/06.png"
        );
        frames[7] = trySetup(
            "/res/objects/coin/coin7.png",
            "/res/objects/coin/coin6.png",
            "/res/objects/coin/06.png"
        );
        frames[8] = trySetup(
            "/res/objects/coin/coin8.png",
            "/res/objects/coin/coin6.png",
            "/res/objects/coin/06.png"
        );

        // Fallback: if some frames are null, compact the array to only non-null ones
        int count = 0;
        for (BufferedImage f : frames) if (f != null) count++;
        if (count == 0) {
            // As a last resort, reuse a single placeholder so it still renders
            frames = new BufferedImage[] { setup("/res/objects/coin/coin0.png") };
        } else if (count < frames.length) {
            BufferedImage[] compact = new BufferedImage[count];
            int idx = 0;
            for (BufferedImage f : frames) if (f != null) compact[idx++] = f;
            frames = compact;
        }

        // Create a looped spin animation (smaller speed = faster)
        animation = new OBJ_Animation(frames, 10);
        down1 = frames[0]; // initial frame for base draw fallback
    }

    // Try multiple candidate paths; returns first successfully loaded image or null
    private BufferedImage trySetup(String... paths) {
        for (String p : paths) {
            BufferedImage img = setup(p);
            if (img != null) return img;
        }
        return null;
    }

    @Override
    public void update() {
        if (animation != null) {
            animation.update();
            BufferedImage cur = animation.getCurrentFrame();
            if (cur != null) {
                down1 = cur;
            }
        }
        // Don't call super.update() to avoid unintended movement logic for items
    }

    public void use(Entity entity) {
        gp.playSoundEffect(1);
        gp.ui.addMessage("Coin +" + value);
        gp.player.coin += value;
    }

    @Override
    public void draw(Graphics2D g2, GamePanel gp) {
        // Draw only if on screen
        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            BufferedImage img = down1 != null ? down1 : image;
            if (img != null) {
                int coinSize = (int)(gp.tileSize * 0.6); // reduce size to 60% of tile
                int offsetX = (gp.tileSize - coinSize) / 2;
                int offsetY = (gp.tileSize - coinSize) / 2;
                g2.drawImage(img, screenX + offsetX, screenY + offsetY, coinSize, coinSize, null);
            }
        }
    }
}