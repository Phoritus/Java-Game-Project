package src.object;

import src.entity.Entity;
import src.main.GamePanel;
import src.main.UtilityTool;

public class OBJ_Heart extends Entity {

    UtilityTool uTool = new UtilityTool();

    public OBJ_Heart(GamePanel gp) {
        super(gp);
        name = "Heart";
        image = setup("/res/objects/heart/heart_full.png");
        image2 = setup("/res/objects/heart/heart_half.png");
        image3 = setup("/res/objects/heart/heart_blank.png");
        int heartSize = (int) (gp.tileSize * 1.5);
        image = uTool.scaleImage(image, heartSize, heartSize);
        image2 = uTool.scaleImage(image2, heartSize, heartSize);
        image3 = uTool.scaleImage(image3, heartSize, heartSize);
    }
}
