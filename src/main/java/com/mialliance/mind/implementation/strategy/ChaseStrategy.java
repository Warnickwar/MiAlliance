package com.mialliance.mind.implementation.strategy;

import com.mialliance.mind.base.IStrategy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

import java.util.function.Supplier;

public class ChaseStrategy implements IStrategy {

    private final PathfinderMob host;
    private final double speedMod;
    private final boolean requiresLoS;
    private int ticksUntilPathRecalculation;

    private final Supplier<Double> distanceToEnd;

    public ChaseStrategy(PathfinderMob host, double speedMod, boolean requiresLoS) {
        this(host, speedMod, requiresLoS, () -> 2.0D);
    }

    public ChaseStrategy(PathfinderMob host, double speedMod, boolean requiresLoS, Supplier<Double> distanceToEnd) {
        this.host = host;
        this.speedMod = speedMod;
        this.requiresLoS = requiresLoS;
        this.ticksUntilPathRecalculation = 0;
        this.distanceToEnd = distanceToEnd;
    }

    @Override
    public void start() {
        this.ticksUntilPathRecalculation = 0;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.host.getTarget();
        if (livingentity != null) {
            this.host.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            double d0 = this.host.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
            this.ticksUntilPathRecalculation = Math.max(this.ticksUntilPathRecalculation - 1, 0);
            if ((this.requiresLoS || this.host.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilPathRecalculation <= 0 && livingentity.distanceToSqr(this.host.position()) >= 1.0D || this.host.getRandom().nextFloat() < 0.05F) {
                this.ticksUntilPathRecalculation = 4 + this.host.getRandom().nextInt(7);
                if (d0 > 1024.0D) {
                    this.ticksUntilPathRecalculation += 10;
                } else if (d0 > 256.0D) {
                    this.ticksUntilPathRecalculation += 5;
                }

                if (!this.host.getNavigation().moveTo(livingentity, this.speedMod)) {
                    this.ticksUntilPathRecalculation += 15;
                }

                this.ticksUntilPathRecalculation = this.adjustedTickDelay(this.ticksUntilPathRecalculation);
            }
        }
    }

    @Override
    public void stop(boolean successful) {
        this.host.getNavigation().stop();
    }

    @Override
    public boolean canPerform() {
        return this.host.getTarget() != null;
    }

    @Override
    public boolean isComplete() {
        assert this.host.getTarget() != null;
        return this.host.position().closerThan(this.host.getTarget().position(), distanceToEnd.get());
    }

}
