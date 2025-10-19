package com.mialliance.mind.implementation.strategy;

import net.minecraft.world.entity.PathfinderMob;

public class EntityIdleStrategy extends IdleStrategy {

    private final PathfinderMob mob;

    public EntityIdleStrategy(PathfinderMob mob, int lowerBound, int upperBound) {
        super(lowerBound, upperBound);
        this.mob = mob;
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
        super.start();
    }

}
