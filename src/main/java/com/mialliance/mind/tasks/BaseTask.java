package com.mialliance.mind.tasks;

import com.mialliance.mind.memories.ImmutableMemoryManager;
import com.mialliance.mind.memories.MemoryValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public abstract class BaseTask {

    private final String identifier;

    private final Map<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions;

    BaseTask(@NotNull String identifier, @NotNull HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions) {
        this.identifier = identifier;
        this.preconditions = preconditions;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    public boolean isUsable(@NotNull ImmutableMemoryManager manager) {
        if (preconditions.isEmpty()) return true;

        AtomicBoolean bool = new AtomicBoolean(true);
        preconditions.forEach((type, expect) -> {
            if (bool.get()) {
                bool.set(manager.compareMemory(type, expect));
            }
        });
        return bool.get();
    }
}
