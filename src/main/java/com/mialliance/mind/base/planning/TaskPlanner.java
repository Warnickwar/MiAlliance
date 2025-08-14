package com.mialliance.mind.base.planning;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.memories.MemoryManager;
import com.mialliance.mind.base.tasks.BaseTask;
import com.mialliance.mind.base.tasks.CompoundTask;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

/**
 * Code inspiration and help obtained from @1mangomaster1 on Discord, and
 * 1mangomaster1 on GitHub (found <a href="https://github.com/1Mangomaster1">here</a>)
 */
public final class TaskPlanner {

    @SuppressWarnings("unchecked")
    public static <O extends MindOwner> TaskPlan<O> makePlan(@NotNull O owner) {
        LinkedList<PrimitiveTask<O>> currentTasks = new LinkedList<>();
        step((BaseTask<O>) owner.getAgent().getDomain(), MemoryManager.of(owner.getAgent().getMemories()), currentTasks);

        return new TaskPlan<>(owner, currentTasks);
    }

    // Public solely for capability to add more CompoundStates.
    public static <O extends MindOwner> boolean step(BaseTask<O> task, @NotNull MemoryManager state, @NotNull LinkedList<PrimitiveTask<O>> currentTasks) {
        // TODO: When reaching higher Java version, switch to
        //  implicit casting Switch statements.
        if (task instanceof CompoundTask<O> cTask) {
            return cTask.getState().attemptStep(cTask, state, currentTasks);
        } else if (task instanceof PrimitiveTask<O> pTask) {
            return primitiveStep(pTask, state, currentTasks);
        } else {
            throw new RuntimeException("(MiAlliance) TaskPlanner cannot step into unknown Task Type! Are you sure you inherited the right class?");
        }
    }

    // No need to have this exposed really
    private static <O extends MindOwner> boolean primitiveStep(@NotNull PrimitiveTask<O> task, @NotNull MemoryManager state, @NotNull LinkedList<PrimitiveTask<O>> currentTasks) {
        if (!task.isUsable(state)) return false;

        task.applyEffectsToState(state);
        currentTasks.add(task);

        return true;
    }

}
