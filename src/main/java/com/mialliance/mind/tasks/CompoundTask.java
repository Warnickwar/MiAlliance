package com.mialliance.mind.tasks;

import com.google.common.collect.ImmutableList;
import com.mialliance.mind.memories.MemoryValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public final class CompoundTask<O extends TaskOwner> extends BaseTask<O> {

    private final CompoundState<O> state;
    private final LinkedList<BaseTask<O>> children;

    public CompoundTask(@NotNull String identifier, @NotNull CompoundState<O> state, @NotNull HashMap<MemoryModuleType<?>, Predicate<MemoryValue<?>>> preconditions, LinkedList<BaseTask<O>> children) {
        super(identifier, preconditions);
        this.state = state;
        this.children = children;
    }

    public Optional<BaseTask<O>> findChild(String identifier) {
        for (BaseTask<O> task : children) {
            if (Objects.equals(task.getIdentifier(), identifier)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    public CompoundState<O> getState() {
        return this.state;
    }

    public List<BaseTask<O>> getChildren() {
        return ImmutableList.copyOf(children);
    }

}
