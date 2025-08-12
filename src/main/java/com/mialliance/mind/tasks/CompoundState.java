package com.mialliance.mind.tasks;

import com.mialliance.mind.memories.ImmutableMemoryManager;

import java.util.Collection;
import java.util.List;

public final class CompoundState {

    // Technically, it's not necessary to *not* have this as an Enumeration,
    //  But I might as well offer the capability to.
    public static final CompoundState SEQUENTIAL = new CompoundState((tasks, owner) -> tasks.stream().allMatch(task -> task.isUsable(owner)));
    public static final CompoundState FALLBACK = new CompoundState((tasks, owner) -> tasks.stream().anyMatch(task -> task.isUsable(owner)));


    // <------------------->

    private final TaskCheck predicate;

    public CompoundState(TaskCheck func) {
        this.predicate = func;
    }

    public boolean isValidToPick(BaseTask[] tasks, ImmutableMemoryManager manager) {
        return isValidToPick(List.of(tasks), manager);
    }

    public boolean isValidToPick(Collection<BaseTask> tasks, ImmutableMemoryManager manager) {
        return predicate.canRun(tasks, manager);
    }

    @FunctionalInterface
    public interface TaskCheck {
        boolean canRun(Collection<BaseTask> tasks, ImmutableMemoryManager owner);
    }

}
