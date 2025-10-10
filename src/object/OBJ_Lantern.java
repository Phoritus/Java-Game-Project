package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Lantern extends Entity {
    public OBJ_Lantern(GamePanel gp) {
        super(gp);

        type = TYPE_LIGHT;
        name = "Lantern";
        down1 = setup("/res/objects/lantern.png");
        price = 20;
        lightRadius = 250; // Radius of light emitted by the lantern
        description = "[" + name + "]\nA lantern that emits light.\nLight Radius +" + lightRadius;

    }
}