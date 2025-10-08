package com.mialliance.mind.base.planning;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import com.mialliance.mind.base.tasks.TaskState;
import com.mialliance.mind.base.tasks.identifiers.TaskIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public final class TaskPlan<O extends MindOwner> {

    private final O owner;
    // I would use a LinkedHashSet, but somehow I can't pull anything after using it?
    private final LinkedList<PrimitiveTask<O>> tasks;
    private final LinkedList<TaskIdentifier> identifers;

    private PrimitiveTask<O> currentTask;

    public TaskPlan(@NotNull O owner, @NotNull LinkedList<PrimitiveTask<O>> tasks, @NotNull LinkedList<TaskIdentifier> identifers) {
        this.owner = owner;
        this.tasks = tasks;
        this.identifers = identifers;
        this.currentTask = null;
    }

    public void tick() {
        if (isComplete()) return;

        if (currentTask == null) {
            if (!next()) return;
        }

        TaskState result = currentTask.tick(owner);
        switch (result) {
            case SUCCESS -> next();
            case FAILURE -> end();
            // Ignore the PROCESSING state-nothing changes
        }
    }

    public boolean isComplete() {
        return tasks.isEmpty() && currentTask == null;
    }

    private boolean next() {
        if (currentTask != null) {
            currentTask.end(owner);
        }

        if (tasks.isEmpty()) {
            currentTask = null;
            return false;
        }

        currentTask = tasks.remove();
        if (currentTask != null) {
            if (!currentTask.start(owner)) {
                end();
                return false;
            }
            return true;
        }
        return false;
    }

    public void end() {
        if (currentTask != null) currentTask.end(owner);

        tasks.clear();
        currentTask = null;
    }

}
