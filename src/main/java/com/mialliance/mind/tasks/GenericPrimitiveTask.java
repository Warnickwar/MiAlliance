package com.mialliance.mind.tasks;

import com.mialliance.mind.memories.MemoryValue;
import com.mialliance.mind.memories.TemplateValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class GenericPrimitiveTask<O extends TaskOwner> extends PrimitiveTask<O> {

    private final PrimitiveRun<O> onStart;
    private final PrimitiveTick<O> onTick;
    private final PrimitiveRun<O> onEnd;

    public GenericPrimitiveTask(@NotNull String identifier, @NotNull HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects,
                         PrimitiveRun<O> onStart, PrimitiveTick<O> onTick, PrimitiveRun<O> onEnd) {
        super(identifier, preconditions, effects);
        this.onStart = onStart;
        this.onTick = onTick;
        this.onEnd = onEnd;
    }

    @Override
    public void start(O owner) {
        this.onStart.run(owner);
    }

    @Override
    public TaskStates tick(O owner) {
        return this.onTick.run(owner);
    }

    @Override
    public void end(O owner) {
        this.onEnd.run(owner);
    }

    @FunctionalInterface
    public interface PrimitiveRun<O extends TaskOwner> {
        void run(O owner);
        PrimitiveRun<?> EMPTY = (PrimitiveRun<TaskOwner>) owner -> {};
    }

    @FunctionalInterface
    public interface PrimitiveTick<O extends TaskOwner> {
        TaskStates run(O owner);
        PrimitiveTick<?> EMPTY = (PrimitiveTick<TaskOwner>) owner -> TaskStates.FAILURE;
    }

}
