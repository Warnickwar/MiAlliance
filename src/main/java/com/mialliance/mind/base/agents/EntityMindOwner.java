package com.mialliance.mind.base.agents;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface EntityMindOwner<E extends LivingEntity> extends MindOwner {
    @NotNull E getEntity();
}
