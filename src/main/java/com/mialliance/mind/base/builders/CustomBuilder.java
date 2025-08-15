package com.mialliance.mind.base.builders;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.BaseTask;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class CustomBuilder<O extends MindOwner, T extends BaseTask<O>> extends BehaviorTreeBuilder<O, T> {

    private final Map<MemoryModuleType<?>, TemplateValue<?>> effects;

    private final TaskFactory<O,T> factory;

    public CustomBuilder(String identifier, TaskFactory<O,T> supplier) {
        super(identifier);
        this.factory = supplier;
        this.effects = new HashMap<>();
    }

    @NotNull
    public <V> CustomBuilder<O,T> addPrecondition(@NotNull MemoryModuleType<V> type, NullablePredicate<V> precondition) {
        addPrecondition(this, type, precondition);
        return this;
    }

    @NotNull
    public <V> CustomBuilder<O,T> addEffect(@NotNull MemoryModuleType<V> type, V value) {
        effects.put(type, TemplateValue.addMemory(type, value));
        return this;
    }

    @NotNull
    private CustomBuilder<O,T> addEffect(@NotNull MemoryModuleType<?> type, @NotNull TemplateValue<?> val) {
        effects.put(type, val);
        return this;
    }

    @Override
    T build() {
        return factory.get(identifier, this.preconditions, this.effects);
    }

    public interface TaskFactory<O extends MindOwner, T extends BaseTask<O>> {
        T get(String id, Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, Map<MemoryModuleType<?>, TemplateValue<?>> effects);
    }
}