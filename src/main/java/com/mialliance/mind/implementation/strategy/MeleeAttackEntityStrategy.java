package com.mialliance.mind.implementation.strategy;

import com.mialliance.Constants;
import com.mialliance.mind.base.IStrategy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

public class MeleeAttackEntityStrategy implements IStrategy {

    private final PathfinderMob host;
    private int ticksUntilNextAttack;

    public MeleeAttackEntityStrategy(PathfinderMob host) {
        this.host = host;
        this.ticksUntilNextAttack = 0;
    }

    @Override
    public void start() {
        this.ticksUntilNextAttack = 0;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.host.getTarget();
        if (livingentity != null) {
            this.host.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            double d0 = this.host.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());

            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(livingentity, d0);
        }
    }

    @Override
    public void stop(boolean successful) {
        if (successful) {
            this.host.setTarget(null);
            this.host.setAggressive(false);
        }
        this.host.getNavigation().stop();
    }

    @Override
    public boolean canPerform() {
        if (this.host.getTarget() == null) return false;
        if (!this.host.getSensing().hasLineOfSight(this.host.getTarget())) return false;
        double reqDistance = Constants.getAttackReachSqrt(this.host, this.host.getTarget());
        return this.host.distanceToSqr(this.host.getTarget()) <= reqDistance;
    }

    @Override
    public boolean isComplete() {
        LivingEntity livingentity = this.host.getTarget();
        if (livingentity == null || !livingentity.isAlive()) {
            return true;
        }
        return ((livingentity instanceof Player player) && (player.isCreative() || player.isSpectator()));
    }

    protected void checkAndPerformAttack(LivingEntity target, double currentDistance) {
        double d0 = Constants.getAttackReachSqrt(this.host, target);
        if (currentDistance <= d0 && this.ticksUntilNextAttack <= 0) {
            this.resetAttackCooldown();
            this.host.swing(InteractionHand.MAIN_HAND);
            this.host.doHurtTarget(target);
        }

    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = Constants.adjustedTickDelay(20);
    }

}
