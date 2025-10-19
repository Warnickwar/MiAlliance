package com.mialliance.mind.implementation.agent;

import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.plan.ActionPlan;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

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

    @Override
    protected ActionPlan planSupplier(MindAgent<T> agent, HashSet<MindGoal> goals, @Nullable MindGoal lastGoal) {
        return this.planner.plan(agent, goals, lastGoal, this.getOwner().getLevel().getProfiler());
    }

}
