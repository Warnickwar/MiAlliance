package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.ImmutableMemoryManager;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseTask<O extends MindOwner> {

    private final String identifier;

    private final Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions;

    BaseTask(@NotNull String identifier, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions) {
        this.identifier = identifier;
        this.preconditions = preconditions;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @SuppressWarnings("unchecked")
    public final <T> boolean isUsable(@NotNull ImmutableMemoryManager manager) {
        if (preconditions.isEmpty()) return true;

        AtomicBoolean bool = new AtomicBoolean(true);
        preconditions.forEach((type, expect) -> {
            if (bool.get()) {
                bool.set(manager.compareMemory((MemoryModuleType<T>) type, (NullablePredicate<T>) expect));
            }
        });
        return bool.get();
    }
}
