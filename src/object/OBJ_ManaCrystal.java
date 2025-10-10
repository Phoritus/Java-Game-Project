package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_ManaCrystal extends Entity {
    
    public OBJ_ManaCrystal(GamePanel gp) {
        super(gp);
        this.gp = gp;

        type = TYPE_PICKUP_ONLY;
        name = "Mana Crystal";
        value = 1;
        down1 = setup("/res/objects/mana1.png");
        image = setup("/res/objects/mana1.png");
        image2 = setup("/res/objects/mana0.png");
        
    }
     public boolean use(Entity entity) {
        gp.playSoundEffect(2);
        gp.ui.addMessage("Mana +" + value);
        entity.mana += value;

        return true;
    }
}