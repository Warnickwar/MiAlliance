package com.mialliance.mind.tasks;

import com.google.common.collect.ImmutableList;
import com.mialliance.mind.agents.MindOwner;
import com.mialliance.mind.memories.MemoryValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public final class CompoundTask<O extends MindOwner> extends BaseTask<O> {

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

    public <T extends BaseTask<O>> void addChild(T task) {
        this.children.add(task);
    }

    public <T extends BaseTask<O>> void addChild(T task, int index) {
        this.children.add(Mth.clamp(index, 0, this.children.size()), task);
    }

    public boolean hasChild(String identifier) {
        return this.findChild(identifier).isPresent();
    }

    public Optional<BaseTask<O>> removeChild(String identifier) {
        Optional<BaseTask<O>> task;
        if ((task = this.findChild(identifier)).isPresent()) {
            this.children.remove(task.get());
        }
        return task;
    }

    public CompoundState<O> getState() {
        return this.state;
    }

    public List<BaseTask<O>> getChildren() {
        return ImmutableList.copyOf(children);
    }

}
