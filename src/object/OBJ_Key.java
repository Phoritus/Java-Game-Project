package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Key extends Entity {
    
    public OBJ_Key(GamePanel gp) {
        super(gp);
        name = "Key";        
        down1 = setup("/res/objects/key/keys_1_1.png");
    }
}
