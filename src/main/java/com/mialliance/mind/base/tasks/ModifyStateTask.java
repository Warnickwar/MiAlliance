package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.memories.TemplateValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ModifyStateTask extends PrimitiveTask<MindOwner> {

    private final Set<TemplateValue<?>> changes;

    public ModifyStateTask(@NotNull String identifier, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions, effects);
        this.changes = new HashSet<>();
    }

    @NotNull
    public <T> ModifyStateTask addMemoryChange(@NotNull MemoryModuleType<T> key, @NotNull T value, long expiryTime) {
        changes.add(TemplateValue.additiveExpirableMemory(key, value, expiryTime));
        return this;
    }

    @NotNull
    public <T> ModifyStateTask addMemoryChange(@NotNull MemoryModuleType<T> key, @NotNull T value) {
        return this.addMemoryChange(key, value, Long.MAX_VALUE);
    }

    @NotNull
    public <T> ModifyStateTask addMemoryRemoval(@NotNull MemoryModuleType<T> key) {
        changes.add(TemplateValue.removableMemory(key));
        return this;
    }

    @NotNull
    public <T> ModifyStateTask addMemoryTemplate(@NotNull TemplateValue<T> templateValue) {
        changes.add(templateValue);
        return this;
    }

    @Override
    public boolean start(MindOwner owner) {
        MemoryManager manager = owner.getAgent().getMemories();
        changes.forEach(val -> val.applyToMemories(manager));
        return true;
    }

    @Override
    public TaskState tick(MindOwner owner) {
        return TaskState.SUCCESS;
    }

    @Override
    public void end(MindOwner owner) {}

}
