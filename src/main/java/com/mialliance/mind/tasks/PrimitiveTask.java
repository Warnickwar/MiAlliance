package com.mialliance.mind.tasks;

import com.mialliance.mind.memories.MemoryManager;
import com.mialliance.mind.memories.MemoryValue;
import com.mialliance.mind.memories.TemplateValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class PrimitiveTask<O extends TaskOwner> extends BaseTask {

    private final Map<MemoryModuleType<?>, TemplateValue<?>> effects;

    PrimitiveTask(@NotNull String identifier, @NotNull HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions);
        this.effects = effects;
    }

    public abstract void start(O owner);

    public abstract TaskStates tick(O owner);

    public abstract void end(O owner);

    public void applyEffectsToState(MemoryManager manager) {
        effects.forEach((type, val) -> val.applyToMemories(manager));
    }
}
