package com.mialliance.mind.base.agents;

import net.minecraft.world.entity.LivingEntity;

public interface EntityMindOwner<E extends LivingEntity> extends MindOwner {
    E getOwner();
}
