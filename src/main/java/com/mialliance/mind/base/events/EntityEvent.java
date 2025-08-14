package com.mialliance.mind.base.events;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class EntityEvent implements IEvent {

    @NotNull
    private final Entity entity;

    public EntityEvent(@NotNull Entity ent) {
        this.entity = ent;
    }

    @NotNull
    public Entity getEntity() {
        return entity;
    }
}
