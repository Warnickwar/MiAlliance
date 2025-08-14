package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.memories.TemplateValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class PrimitiveTask<O extends MindOwner> extends BaseTask<O> {

    private final Map<MemoryModuleType<?>, TemplateValue<?>> effects;

    protected PrimitiveTask(@NotNull String identifier, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions);
        this.effects = effects;
    }

    public abstract boolean start(O owner);

    public abstract TaskState tick(O owner);

    public abstract void end(O owner);

    public final void applyEffectsToState(MemoryManager manager) {
        effects.forEach((type, val) -> val.applyToMemories(manager));
    }
}
