package com.mialliance.mind.implementation.strategy;

import com.mialliance.mind.base.strategy.IStrategy;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;

public class RangeAttackEntityStrategy implements IStrategy {

    private final PathfinderMob host;
    private final double speedModifier;
    private int ticksUntilNextAttack;
    private int ticksUntilPathRecalculation;

    public RangeAttackEntityStrategy(PathfinderMob host, double speedMod, float orbitDistance) {
        this.host = host;
        this.speedModifier = speedMod;
        this.ticksUntilNextAttack = 0;
        this.ticksUntilPathRecalculation = 0;
    }

    @Override
    public void start() {
        if (this.host.getTarget() != null) {

        }
        this.host.setAggressive(true);
    }

    @Override
    public void tick() {
        LivingEntity target = this.host.getTarget();
        if (target == null) return;

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

    public void performRangedAttack(LivingEntity target) {
        ItemStack itemstack = host.getProjectile(host.getItemInHand(ProjectileUtil.getWeaponHoldingHand(host, item -> item instanceof net.minecraft.world.item.BowItem)));
        AbstractArrow abstractarrow = new Arrow(host.level, host);
        double d0 = target.getX() - host.getX();
        double d1 = target.getY(0.3333333333333333D) - abstractarrow.getY();
        double d2 = target.getZ() - host.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        abstractarrow.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - host.level.getDifficulty().getId() * 4));
        host.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (host.getRandom().nextFloat() * 0.4F + 0.8F));
        host.level.addFreshEntity(abstractarrow);
    }

}
