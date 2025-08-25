package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Normal_Sword extends Entity {
    public OBJ_Normal_Sword(GamePanel gp) {
        super(gp);
        name = "Normal Sword";
        down1 = setup("/res/objects/sword.png");
        attackValue = 4;
        description = "[" + name + "]\nA standard sword.\nAttack +" + attackValue;
     }
}
