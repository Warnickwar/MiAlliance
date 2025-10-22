package com.mialliance.mind.implementation.sensor;

import com.mialliance.mind.base.MindSensor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RetaliateSensor extends MindSensor {

    private static final TargetingConditions RETALIATE_CONDITIONS = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();

    private final Mob host;
    private final boolean mustReach;
    private final Class<?>[] toIgnore;

    private ReachState reachCache;
    private int reachCacheTime;

    private enum ReachState {
        UNKNOWN,
        CAN_REACH,
        OUT_OF_REACH
    }

    int lastAttackerTimestamp;

    public RetaliateSensor(@NotNull Mob host, boolean mustReach, Class<?>... ignoreDamage) {
        super(1);
        this.host = host;
        this.lastAttackerTimestamp = 0;
        this.toIgnore = ignoreDamage;
        this.mustReach = mustReach;
        this.reachCache = ReachState.UNKNOWN;
        this.reachCacheTime = 0;
    }

    @Override
    protected void onTick() {
        int timestamp = host.getLastHurtByMobTimestamp();
        LivingEntity attacker = host.getLastHurtByMob();
        if (timestamp != lastAttackerTimestamp && attacker != null) {
            if (attacker.getType() == EntityType.PLAYER && this.host.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return;
            } else {
                for (Class<?> oclass : this.toIgnore) {
                    if (oclass.isAssignableFrom(attacker.getClass())) {
                        return;
                    }
                }

                if (this.canAttack(attacker, RETALIATE_CONDITIONS)) {
                    this.host.setTarget(attacker);
                    this.host.setAggressive(true);
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected boolean canAttack(@Nullable LivingEntity p_26151_, TargetingConditions p_26152_) {
        if (p_26151_ == null) {
            return false;
        } else if (!p_26152_.test(this.host, p_26151_)) {
            return false;
        } else if (!this.host.isWithinRestriction(p_26151_.blockPosition())) {
            return false;
        } else {
            if (this.mustReach) {
                if (--this.reachCacheTime <= 0) {
                    this.reachCache = ReachState.UNKNOWN;
                }

                if (this.reachCache == ReachState.UNKNOWN) {
                    this.reachCache = this.canReach(p_26151_) ? ReachState.CAN_REACH : ReachState.OUT_OF_REACH;
                }

                return this.reachCache != ReachState.OUT_OF_REACH;
            }

            return true;
        }
    }

    private boolean canReach(LivingEntity p_26149_) {
        this.reachCacheTime = reducedTickDelay(10 + this.host.getRandom().nextInt(5));
        Path path = this.host.getNavigation().createPath(p_26149_, 0);
        if (path == null) {
            return false;
        } else {
            Node node = path.getEndNode();
            if (node == null) {
                return false;
            } else {
                int i = node.x - p_26149_.getBlockX();
                int j = node.z - p_26149_.getBlockZ();
                return (double)(i * i + j * j) <= 2.25D;
            }
        }
    }

    private static int reducedTickDelay(int p_186074_) {
        return Mth.positiveCeilDiv(p_186074_, 2);
    }
}
