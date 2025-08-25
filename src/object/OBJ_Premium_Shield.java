package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Premium_Shield extends Entity {
    public OBJ_Premium_Shield(GamePanel gp) {
        super(gp);

        name = "Premium Shield";
        down1 = setup("/res/objects/shield.png");
        defenseValue = 2;
        description = "[" + name + "]\nA premium shield.\nDefense +" + defenseValue;
    }

}
