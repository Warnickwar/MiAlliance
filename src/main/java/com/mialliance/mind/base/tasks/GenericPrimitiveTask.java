package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class GenericPrimitiveTask<O extends MindOwner> extends PrimitiveTask<O> {

    private final PrimitiveStart<O> onStart;
    private final PrimitiveTick<O> onTick;
    private final PrimitiveEnd<O> onEnd;

    public GenericPrimitiveTask(@NotNull String identifier, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects,
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

    public interface PrimitiveStart<O extends MindOwner> {
        boolean run(O owner);
        PrimitiveStart<?> EMPTY = (PrimitiveStart<MindOwner>) owner -> false;
    }

    @FunctionalInterface
    public interface PrimitiveTick<O extends MindOwner> {

        TaskState run(O owner);
        PrimitiveTick<?> EMPTY = (PrimitiveTick<MindOwner>) owner -> TaskState.FAILURE;
    }
    @FunctionalInterface
    public interface PrimitiveEnd<O extends MindOwner> {
        void run(O owner);
        PrimitiveEnd<?> EMPTY = (PrimitiveEnd<MindOwner>) owner -> {};
    }

}
