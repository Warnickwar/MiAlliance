package com.mialliance.mind.base.builders;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.GenericPrimitiveTask;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

// I highly recommend you just make your own Primitive classes.
public class PrimitiveBuilder<O extends MindOwner> extends BehaviorTreeBuilder<O, GenericPrimitiveTask<O>> {

    private GenericPrimitiveTask.PrimitiveStart<O> onStart;
    private GenericPrimitiveTask.PrimitiveTick<O> onTick;
    private GenericPrimitiveTask.PrimitiveEnd<O> onEnd;

    private final Map<MemoryModuleType<?>, TemplateValue<?>> effects;

    @SuppressWarnings("unchecked")
    public PrimitiveBuilder(String identifier) {
        super(identifier);
        this.onStart = (GenericPrimitiveTask.PrimitiveStart<O>) GenericPrimitiveTask.PrimitiveStart.EMPTY;
        this.onTick = (GenericPrimitiveTask.PrimitiveTick<O>) GenericPrimitiveTask.PrimitiveTick.EMPTY;
        this.onEnd = (GenericPrimitiveTask.PrimitiveEnd<O>) GenericPrimitiveTask.PrimitiveEnd.EMPTY;
        effects = new HashMap<>();
    }

    @NotNull
    public <T> PrimitiveBuilder<O> addEffect(@NotNull MemoryModuleType<T> type, @NotNull T value) {
        effects.put(type, TemplateValue.addMemory(type, value));
        return this;
    }

    public PrimitiveBuilder<O> setOnStart(GenericPrimitiveTask.PrimitiveStart<O> task) {
        this.onStart = task;
        return this;
    }

    public PrimitiveBuilder<O> setOnTick(GenericPrimitiveTask.PrimitiveTick<O> task) {
        this.onTick = task;
        return this;
    }

    public PrimitiveBuilder<O> setOnEnd(GenericPrimitiveTask.PrimitiveEnd<O> task) {
        this.onEnd = task;
        return this;
    }

    @Override
    GenericPrimitiveTask<O> build() {
        return new GenericPrimitiveTask<>(identifier, preconditions, effects, onStart, onTick, onEnd);
    }

}