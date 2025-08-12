package com.mialliance.mind.tasks;

import com.mialliance.mind.memories.ImmutableMemoryManager;
import com.mialliance.mind.memories.MemoryValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public final class CompoundTask extends BaseTask {

    private final CompoundState state;
    private final List<BaseTask> children;

    public CompoundTask(@NotNull String identifier, @NotNull CompoundState state, @NotNull HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions, List<BaseTask> children) {
        super(identifier, preconditions);
        this.state = state;
        this.children = children;
    }

    @Override
    public boolean isUsable(@NotNull ImmutableMemoryManager manager) {
        return super.isUsable(manager) && state.isValidToPick(children, manager);
    }

    public Optional<BaseTask> findChild(String identifier) {
        for (BaseTask task : children) {
            if (Objects.equals(task.getIdentifier(), identifier)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

}
