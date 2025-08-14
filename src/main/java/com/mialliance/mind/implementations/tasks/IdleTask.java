package com.mialliance.mind.implementations.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.PathfinderMindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import com.mialliance.mind.base.tasks.TaskState;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class IdleTask extends PrimitiveTask<PathfinderMindOwner>  {

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
    public boolean start(PathfinderMindOwner owner) {
        this.currentIdleTime = Math.max(0, owner.getOwner().getRandom().nextInt(minTicks, maxTicks));
        return false;
    }

    @Override
    public TaskState tick(PathfinderMindOwner owner) {
        if (currentIdleTime-- <= 0) return TaskState.SUCCESS;
        return TaskState.PROCESSING;
    }

    @Override
    public void end(PathfinderMindOwner owner) {}

}
