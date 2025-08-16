package com.mialliance.mind.implementations.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import com.mialliance.mind.base.tasks.TaskState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MeleeAttackTask extends PrimitiveTask<EntityMindOwner<Mob>> {

    private int ticksUntilNextPathRecalculation = 0;
    private int ticksUntilNextAttack = 0;
    protected float speedMultiplier;
    protected boolean canTrackThroughWalls;
    protected boolean penalizable;
    protected int failedPenalty = 0;

    protected MeleeAttackTask(@NotNull String identifier, float speedMultiplier, boolean canTrackThroughWalls, boolean penalizable, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions, effects);
        this.speedMultiplier = speedMultiplier;
        this.canTrackThroughWalls = canTrackThroughWalls;
        this.penalizable = penalizable;
    }

    @Override
    public boolean start(EntityMindOwner<Mob> owner) {
        this.ticksUntilNextAttack = 0;
        this.ticksUntilNextPathRecalculation = 0;
        return owner.getEntity().getTarget() != null;
    }

    @Override
    public TaskState tick(EntityMindOwner<Mob> owner) {
        // Checking because this task could be anywhere in the tree, and the result may be different
        if (owner.getEntity().getTarget() == null) return TaskState.FAILURE;
        Mob mob = owner.getEntity();

        LivingEntity target = owner.getEntity().getTarget();
        if (target != null) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double d0 = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            if ((this.canTrackThroughWalls || mob.getSensing().hasLineOfSight(target)) && this.ticksUntilNextPathRecalculation <= 0 && (target.distanceToSqr(target.getX(), target.getY(), target.getZ()) >= 1.0D || mob.getRandom().nextFloat() < 0.05F)) {
                this.ticksUntilNextPathRecalculation = 4 + mob.getRandom().nextInt(7);
                if (this.penalizable) {
                    this.ticksUntilNextPathRecalculation += failedPenalty;
                    if (mob.getNavigation().getPath() != null) {
                        net.minecraft.world.level.pathfinder.Node finalPathPoint = mob.getNavigation().getPath().getEndNode();
                        if (finalPathPoint != null && target.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                            failedPenalty = 0;
                        else
                            failedPenalty += 10;
                    } else {
                        failedPenalty += 10;
                    }
                }
                if (d0 > 1024.0D) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (d0 > 256.0D) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                if (!mob.getNavigation().moveTo(target, this.speedMultiplier)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }

                this.ticksUntilNextPathRecalculation = Mth.positiveCeilDiv(this.ticksUntilNextPathRecalculation, 2);
            }

            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(mob, target, d0);
        } else {
            return TaskState.SUCCESS;
        }

        return TaskState.PROCESSING;
    }

    @Override
    public void end(EntityMindOwner<Mob> owner) {
        Mob ent = owner.getEntity();
        LivingEntity target = ent.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            ent.setTarget(null);
        }

        ent.setAggressive(false);
        ent.getNavigation().stop();
    }

    protected void checkAndPerformAttack(Mob org, LivingEntity target, double maxDistance) {
        double d0 = this.getAttackReachSqr(org, target);
        if (maxDistance <= d0 && this.ticksUntilNextAttack <= 0) {
            this.ticksUntilNextAttack = Mth.positiveCeilDiv(20, 2);
            org.swing(InteractionHand.MAIN_HAND);
            org.doHurtTarget(target);
        }
    }

    protected double getAttackReachSqr(Mob org, LivingEntity target) {
        return (double)(org.getBbWidth() * 2.0F * org.getBbWidth() * 2.0F + target.getBbWidth());
    }

}
