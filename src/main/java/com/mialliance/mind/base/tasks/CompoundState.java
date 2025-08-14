package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.planning.TaskPlanner;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public record CompoundState<O extends MindOwner>(StepAttempt<O> attemptFunction) {

    public static final CompoundState<?> SEQUENTIAL = new CompoundState<>((cTask, state, currentTasks) -> {
        MemoryManager tempState = MemoryManager.of(state);
        LinkedList<PrimitiveTask<MindOwner>> tempSteps = new LinkedList<>(currentTasks);

        for (BaseTask<MindOwner> task : cTask.getChildren()) {
            if (!task.isUsable(tempState)) return false;
            if (!TaskPlanner.step(task, tempState, tempSteps)) return false;
        }

        state.acceptAll(tempState);

        currentTasks.clear();
        currentTasks.addAll(tempSteps);

        return true;
    });
    public static final CompoundState<?> FALLBACK = new CompoundState<>((cTask, state, currentTasks) -> {
        for (BaseTask<MindOwner> task : cTask.getChildren()) {
            if (!task.isUsable(state)) return false;
            return TaskPlanner.step(task, state, currentTasks);
        }
        return false;
    });

    public boolean attemptStep(@NotNull CompoundTask<O> cTask, @NotNull MemoryManager state, @NotNull LinkedList<PrimitiveTask<O>> currentTasks) {
        return this.attemptFunction.attemptStep(cTask, state, currentTasks);
    }

    @FunctionalInterface
    public interface StepAttempt<O extends MindOwner> {

        boolean attemptStep(@NotNull CompoundTask<O> cTask, @NotNull MemoryManager state, @NotNull LinkedList<PrimitiveTask<O>> currentTasks);

    }

}
