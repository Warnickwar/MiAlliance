package com.mialliance.mind.base.builders;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.ModifyStateTask;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModifyStateBuilder extends BehaviorTreeBuilder<MindOwner, ModifyStateTask> {

    private final Map<MemoryModuleType<?>, TemplateValue<?>> effects;
    private final Set<TemplateValue<?>> memoryChanges;

    public ModifyStateBuilder(String identifier) {
        super(identifier);
        this.effects = new HashMap<>();
        this.memoryChanges = new HashSet<>();
    }

    @NotNull
    public <T> ModifyStateBuilder addPrecondition(@NotNull MemoryModuleType<T> type, @NotNull NullablePredicate<T> precondition) {
        addPrecondition(this, type, precondition);
        return this;
    }

    @NotNull
    public <T> ModifyStateBuilder addEffect(@NotNull MemoryModuleType<T> type, @NotNull T value) {
        effects.put(type, TemplateValue.additiveMemory(type, value));
        return this;
    }

    @NotNull
    public <T> ModifyStateBuilder addMemoryChange(@NotNull MemoryModuleType<T> type, @NotNull T value, long expiryTime) {
        memoryChanges.add(TemplateValue.additiveExpirableMemory(type, value, expiryTime));
        return this;
    }

    @NotNull
    public <T> ModifyStateBuilder addMemoryChange(@NotNull MemoryModuleType<T> type, @NotNull T value) {
        return this.addMemoryChange(type, value, Long.MAX_VALUE);
    }

    @NotNull
    public <T> ModifyStateBuilder addMemoryRemoval(@NotNull MemoryModuleType<T> type) {
        memoryChanges.add(TemplateValue.removableMemory(type));
        return this;
    }

    @Override
    ModifyStateTask build() {
        ModifyStateTask task = new ModifyStateTask(identifier, preconditions, effects);
        memoryChanges.forEach(templateValue ->  {
            task.addMemoryTemplate(templateValue.copy());
        });
        return task;
    }

}
