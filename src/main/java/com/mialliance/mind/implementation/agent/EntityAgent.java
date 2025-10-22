package com.mialliance.mind.implementation.agent;

import com.mialliance.mind.base.agent.MindAgent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class EntityAgent<T extends PathfinderMob> extends MindAgent<T> {

    public EntityAgent() {}

    public boolean isType(EntityType<?> type) {
        return getOwner().getType() == type;
    }

    @Override
    public Vec3 getLocation() {
        return getOwner().position();
    }

    @Override
    public Level getLevel() {
        return getOwner().level;
    }

    @Override
    protected void onPlanFinish() {
        getOwner().getNavigation().stop();
    }

}
