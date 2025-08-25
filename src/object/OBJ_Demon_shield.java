
package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Demon_shield extends Entity {

    public OBJ_Demon_shield(GamePanel gp) {
        super(gp);
        name = "Demon Shield";
        type = TYPE_SHIELD;
        down1 = setup("/res/objects/demon_shield.png");
        defense = 5;
        description = "[ " + name + " ]\n" +
                      "A shield made from\ndemon scales.";
    }
}