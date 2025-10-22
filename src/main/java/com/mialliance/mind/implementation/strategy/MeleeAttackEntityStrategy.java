package com.mialliance.mind.implementation.strategy;

import com.mialliance.mind.base.IStrategy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

public class MeleeAttackEntityStrategy implements IStrategy {

    private final PathfinderMob host;
    private final double speedModifier;
    private final boolean followNoLOS;
    private int ticksUntilNextAttack;
    private int ticksUntilPathRecalculation;

    public MeleeAttackEntityStrategy(PathfinderMob host, double speedMod, boolean followNoLOS) {
        this.host = host;
        this.speedModifier = speedMod;
        this.followNoLOS = followNoLOS;
        this.ticksUntilNextAttack = 0;
        this.ticksUntilPathRecalculation = 0;
    }

    @Override
    public void start() {
        this.ticksUntilNextAttack = 0;
        this.ticksUntilPathRecalculation = 0;
    }

    @Override
    public void tick() {
        LivingEntity livingentity = this.host.getTarget();
        if (livingentity != null) {
            this.host.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
            double d0 = this.host.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
            this.ticksUntilPathRecalculation = Math.max(this.ticksUntilPathRecalculation - 1, 0);
            if ((this.followNoLOS || this.host.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilPathRecalculation <= 0 && livingentity.distanceToSqr(this.host.position()) >= 1.0D || this.host.getRandom().nextFloat() < 0.05F) {
                this.ticksUntilPathRecalculation = 4 + this.host.getRandom().nextInt(7);
                if (d0 > 1024.0D) {
                    this.ticksUntilPathRecalculation += 10;
                } else if (d0 > 256.0D) {
                    this.ticksUntilPathRecalculation += 5;
                }

                if (!this.host.getNavigation().moveTo(livingentity, this.speedModifier)) {
                    this.ticksUntilPathRecalculation += 15;
                }

                this.ticksUntilPathRecalculation = this.adjustedTickDelay(this.ticksUntilPathRecalculation);
            }

            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(livingentity, d0);
        }
    }

    @Override
    public void stop(boolean successful) {
        if (successful) {
            LivingEntity livingentity = this.host.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.host.setTarget(null);
            }

            this.host.setAggressive(false);
        }
        this.host.getNavigation().stop();
    }

    @Override
    public boolean canPerform() {
        return this.host.getTarget() != null;
    }

    @Override
    public boolean isComplete() {
        LivingEntity livingentity = this.host.getTarget();
        if (livingentity == null || !livingentity.isAlive()) {
            return true;
        }
        return ((livingentity instanceof Player player) && (player.isCreative() || player.isSpectator()));
    }

    protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
        double d0 = this.getAttackReachSqr(p_25557_);
        if (p_25558_ <= d0 && this.ticksUntilNextAttack <= 0) {
            this.resetAttackCooldown();
            this.host.swing(InteractionHand.MAIN_HAND);
            this.host.doHurtTarget(p_25557_);
        }

    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(20);
    }

    protected double getAttackReachSqr(LivingEntity p_25556_) {
        return (double)(this.host.getBbWidth() * 2.0F * this.host.getBbWidth() * 2.0F + p_25556_.getBbWidth());
    }

}
