package com.mialliance.mind.base.agent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public interface EntityMindAgentHolder extends MindAgentHolder {
    Entity getEntity();
    EntityType<?> getType();
}
