package com.mialliance.components;

import net.minecraft.world.entity.Entity;

public interface EntityComponentObject<E extends Entity> extends ComponentObject {
    E getEntity();
}
