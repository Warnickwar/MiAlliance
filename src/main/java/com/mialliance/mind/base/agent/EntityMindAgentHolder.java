package com.mialliance.mind.base.agent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public interface EntityMindAgentHolder extends MindAgentHolder {
    Entity getEntity();
    PathNavigation getNavigation();
    EntityType<?> getType();
}
