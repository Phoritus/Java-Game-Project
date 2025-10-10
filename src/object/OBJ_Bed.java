package src.object;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Bed extends Entity {
    GamePanel gp;

    public OBJ_Bed(GamePanel gp) {
        super(gp);
        this.gp = gp;

        type = TYPE_CONSUMABLE;
        name = "Bed";
        down1 = setup("/res/objects/bed.png");
        price = 50;
        description = "[" + name + "]\nA comfy bed.\nRestores health when used.";
    }

    public boolean use(Entity entity) {
        gp.gameState = gp.sleepState;
        gp.playSoundEffect(15); // Play sleep sound effect
        gp.player.life = gp.player.maxLife; // Restore player's health to max
        gp.player.mana = gp.player.maxMana; // Restore player's mana to max

        gp.ui.addMessage("You feel rested!");

        return true;
    }

  
}