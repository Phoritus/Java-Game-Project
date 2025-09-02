package src.object;

import src.entity.Entity;
import src.entity.Projectile;
import src.main.GamePanel;

public class OBJ_Fireball extends Projectile implements src.interfaces.ManaConsumer{

    GamePanel gp;
    
    public OBJ_Fireball(GamePanel gp) {
        super(gp);
        this.gp = gp;

        name = "FireBall";
        speed = 4;
        maxLife = 80;
        life = maxLife;
        attack = 6;
        useCost = 1;
        alive = false;
    getImage();
    // Default directional spawn offsets (pixels) from player center
    // Order: up, down, left, right
    spawnOffsetX = new int[] { 0, 0, -24, 24 };
    spawnOffsetY = new int[] { -2, 2, 0, 0 };
    // Make fireball render larger than one tile
    this.drawScale = 1.5f; // tweak 1.2 - 2.0 as desired
    }


    public void getImage() {
        String base = "/res/projectile/fireball/";
        // Use actual filenames present in res folder (.png)
        up1 = setup(base + "/UFB/FB001.png");
        up2 = setup(base + "/UFB/FB002.png");
        up3 = setup(base + "/UFB/FB003.png");
        up4 = setup(base + "/UFB/FB004.png");
        up5 = setup(base + "/UFB/FB005.png");
        down1 = setup(base + "/DFB/FB001.png");
        down2 = setup(base + "/DFB/FB002.png");
        down3 = setup(base + "/DFB/FB003.png");
        down4 = setup(base + "/DFB/FB004.png");
        down5 = setup(base + "/DFB/FB005.png");
        left1 = setup(base + "/LFB/FB001.png");
        left2 = setup(base + "/LFB/FB002.png");
        left3 = setup(base + "/LFB/FB003.png");
        left4 = setup(base + "/LFB/FB004.png");
        left5 = setup(base + "/LFB/FB005.png");
        right1 = setup(base + "/RFB/FB001.png");
        right2 = setup(base + "/RFB/FB002.png");
        right3 = setup(base + "/RFB/FB003.png");
        right4 = setup(base + "/RFB/FB004.png");
        right5 = setup(base + "/RFB/FB005.png");

    }

    public boolean hasResource(Entity user) {
        return user.mana >= useCost;
    }

    public void subtractResource(Entity user) {
        user.mana -= useCost;
    }
}
