package com.mialliance.mind.base.tasks;

import com.google.common.collect.ImmutableList;
import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class CompoundTask<O extends MindOwner> extends BaseTask<O> {

    private final CompoundState<O> state;
    private final Map<TaskPriority, LinkedList<BaseTask<O>>> children;

    public CompoundTask(@NotNull String identifier, @NotNull CompoundState<O> state, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, Map<TaskPriority, LinkedList<BaseTask<O>>> children) {
        super(identifier, preconditions);
        this.state = state;
        this.children = children;
    }

    public CompoundTask(@NotNull String identifier, @NotNull CompoundState<O> state, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, Collection<TaskInformation<O>> tasks) {
        this(identifier, state, preconditions, decomposeInformation(tasks));
    }

    public TaskSearchResult<O, BaseTask<O>> findChild(String identifier) {
        for (TaskPriority priority : children.keySet()) {
            LinkedList<BaseTask<O>> list = children.get(priority);
            for (BaseTask<O> task : list) {
                if (Objects.equals(task.getIdentifier(), identifier)) {
                    return new TaskSearchResult<>(priority, task);
                }
            }
        }
        return new TaskSearchResult<>(TaskPriority.MEDIUM, null);
    }

    public <T extends BaseTask<O>> void addChild(TaskPriority priority, T task) {
        this.children.add(task);
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

    private void validatePriority(TaskPriority priority) {

    }

    private static <O extends MindOwner> Map<TaskPriority, LinkedList<BaseTask<O>>> decomposeInformation(Collection<TaskInformation<O>> tasks) {
        Map<TaskPriority, LinkedList<BaseTask<O>>> map = new HashMap<>();
        tasks.forEach(info -> {
            if (!map.containsKey(info.priority())) {
                map.put(info.priority(), new LinkedList<>());
            }
            map.get(info.priority()).add(info.task());
        });
        return map;
    }

    public static class TaskSearchResult<O extends MindOwner, T extends BaseTask<O>> {

        private final TaskPriority priority;
        @Nullable
        private final T task;

        TaskSearchResult(TaskPriority priority, @Nullable T task) {
            this.priority = priority;
            this.task = task;
        }

        public boolean isFound() {
            return task != null;
        }

        public TaskPriority getPriority() {
            return this.priority;
        }

        @Nullable
        public T getTask() {
            return this.task;
        }
    }
}
