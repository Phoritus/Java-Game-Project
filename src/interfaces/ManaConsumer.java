package src.interfaces;

import src.entity.Entity;

public interface ManaConsumer {
    boolean hasResource(Entity user);
    void subtractResource(Entity user);
}
