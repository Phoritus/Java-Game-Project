package src.object;

import java.awt.image.BufferedImage;

import src.entity.Entity;
import src.main.GamePanel;

public class OBJ_Cyrstal extends Entity {

  GamePanel gp;
  public static final String objName = "Cyrstal";
  private OBJ_Animation animation;

  public OBJ_Cyrstal(GamePanel gp) {
    super(gp);
    this.gp = gp;

    type = TYPE_PICKUP_ONLY;
    name = objName;
    description = "[" + name + "]\nA mystical cyrstal that\nfully restores life and mana.";

    // Attempt to load animation frames from /res/objects/crystals/c_0.png ...
    // c_23.png
    BufferedImage[] frames = loadCrystalFrames();
    if (frames != null && frames.length > 0) {
      animation = new OBJ_Animation(frames, 6); // smaller => faster
      down1 = frames[0]; // initial frame
    } else {
      // Fallback: single static image (legacy spelling kept)
      down1 = setup("/res/objects/cyrstal.png", gp.tileSize, gp.tileSize);
    }

    // Non-blocking pickup; generous pickup area
    this.collision = false;
    this.solidArea = new java.awt.Rectangle(8, 8, gp.tileSize - 16, gp.tileSize - 16);
    this.solidAreaDefaultX = this.solidArea.x;
    this.solidAreaDefaultY = this.solidArea.y;

    // Dialogue
    setDialogue();
  }

  private BufferedImage[] loadCrystalFrames() {
    // Load frames, compact to only those that exist
    BufferedImage[] tmp = new BufferedImage[24];
    int count = 0;
    for (int i = 0; i < tmp.length; i++) {
      String path = "/res/objects/crystals/c_" + i + ".png";
      BufferedImage img = setup(path, gp.tileSize, gp.tileSize);
      if (img != null) {
        tmp[count++] = img;
      }
    }
    if (count == 0)
      return null;
    BufferedImage[] frames = new BufferedImage[count];
    System.arraycopy(tmp, 0, frames, 0, count);
    return frames;
  }

  @Override
  public void update() {
    if (animation != null) {
      animation.update();
      BufferedImage cur = animation.getCurrentFrame();
      if (cur != null) {
        down1 = cur; // Let base draw() use the current frame
      }
    }
    // Do not call super.update(); items shouldn't move
  }

  public void setDialogue() {
    dialogues[0][0] = "The air shimmers...\nThe crystal hums softly.";
    dialogues[0][1] = "You can feel\nimmense power\nradiating from within.";
    dialogues[0][2] = "Only those who have defeated\nthe guardian can claim\nthis relic.";
    dialogues[0][3] = "You have proven your\nstrength... Take it.";
    dialogues[0][4] = "The Ancient Crystal is\nnow yours.";
  }

  @Override
  public boolean use(Entity entity) {
    gp.gameState = gp.cutsceneState;
    gp.cutsceneManager.sceneNum = gp.cutsceneManager.ending;

    return true;
  }

}
