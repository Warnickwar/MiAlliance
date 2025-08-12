package com.mialliance.mind.tasks;

import com.mialliance.mind.memories.MemoryValue;
import com.mialliance.mind.memories.TemplateValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public final class GenericPrimitiveTask<O extends TaskOwner> extends PrimitiveTask<O> {

    private final PrimitiveStart<O> onStart;
    private final PrimitiveTick<O> onTick;
    private final PrimitiveEnd<O> onEnd;

    public GenericPrimitiveTask(@NotNull String identifier, @NotNull HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects,
                                PrimitiveStart<O> onStart, PrimitiveTick<O> onTick, PrimitiveEnd<O> onEnd) {
        super(identifier, preconditions, effects);
        this.onStart = onStart;
        this.onTick = onTick;
        this.onEnd = onEnd;
    }

    @Override
    public boolean start(O owner) {
        return this.onStart.run(owner);
    }

    @Override
    public TaskState tick(O owner) {
        return this.onTick.run(owner);
    }

    @Override
    public void end(O owner) {
        this.onEnd.run(owner);
    }

    public interface PrimitiveStart<O extends TaskOwner> {
        boolean run(O owner);
        PrimitiveStart<?> EMPTY = (PrimitiveStart<TaskOwner>) owner -> false;
    }

    @FunctionalInterface
    public interface PrimitiveTick<O extends TaskOwner> {

        TaskState run(O owner);
        PrimitiveTick<?> EMPTY = (PrimitiveTick<TaskOwner>) owner -> TaskState.FAILURE;
    }
    @FunctionalInterface
    public interface PrimitiveEnd<O extends TaskOwner> {
        void run(O owner);
        PrimitiveEnd<?> EMPTY = (PrimitiveEnd<TaskOwner>) owner -> {};
    }

}
