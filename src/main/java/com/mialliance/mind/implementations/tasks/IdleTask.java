package com.mialliance.mind.implementations.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import com.mialliance.mind.base.tasks.TaskState;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class IdleTask extends PrimitiveTask<EntityMindOwner<PathfinderMob>>  {

    private final int minTicks;
    private final int maxTicks;
    private int currentIdleTime;

    public IdleTask(@NotNull String identifier, int minimumTime, int maximumTime, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions, effects);
        this.minTicks = minimumTime;
        this.maxTicks = maximumTime;
        this.currentIdleTime = 0;
    }

    @Override
    public boolean start(EntityMindOwner<PathfinderMob> owner) {
        this.currentIdleTime = Math.max(0, owner.getEntity().getRandom().nextInt(minTicks, maxTicks));
        return false;
    }

    @Override
    public TaskState tick(EntityMindOwner<PathfinderMob> owner) {
        if (currentIdleTime-- <= 0) return TaskState.SUCCESS;
        return TaskState.PROCESSING;
    }

    @Override
    public void end(EntityMindOwner<PathfinderMob> owner) {}

}
