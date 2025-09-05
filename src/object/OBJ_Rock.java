package src.object;

import src.entity.Entity;
import src.entity.Projectile;
import src.main.GamePanel;

public class OBJ_Rock extends Projectile {

    GamePanel gp;

    public OBJ_Rock(GamePanel gp) {
        super(gp);
        this.gp = gp;

        name = "Rock";
        speed = 4;
        maxLife = 80;
        life = maxLife;
        attack = 2;
        useCost = 1;
        alive = false;
        getImage();

    }

    public void getImage() {
        // Use actual filenames present in res folder (.png)
        up1 = setup("/res/objects/rock.png");
        up2 = setup("/res/objects/rock.png");
        up3 = setup("/res/objects/rock.png");
        up4 = setup("/res/objects/rock.png");
        up5 = setup("/res/objects/rock.png");
        down1 = setup("/res/objects/rock.png");
        down2 = setup("/res/objects/rock.png");
        down3 = setup("/res/objects/rock.png");
        down4 = setup("/res/objects/rock.png");
        down5 = setup("/res/objects/rock.png");
        left1 = setup("/res/objects/rock.png");
        left2 = setup("/res/objects/rock.png");
        left3 = setup("/res/objects/rock.png");
        left4 = setup("/res/objects/rock.png");
        left5 = setup("/res/objects/rock.png");
        right1 = setup("/res/objects/rock.png");
        right2 = setup("/res/objects/rock.png");
        right3 = setup("/res/objects/rock.png");
        right4 = setup("/res/objects/rock.png");
        right5 = setup("/res/objects/rock.png");

    }

    public boolean hasResource(Entity user) {
        return user.ammo >= useCost;
    }

    public void subtractResource(Entity user) {
        user.ammo -= useCost;
    }
}
