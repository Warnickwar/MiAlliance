package com.mialliance.mind.planning;

import com.mialliance.mind.tasks.PrimitiveTask;
import com.mialliance.mind.tasks.TaskOwner;
import com.mialliance.mind.tasks.TaskState;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class TaskPlan<O extends TaskOwner> {

    private final O owner;
    // I would use a LinkedHashSet, but somehow I can't pull anything after using it?
    private final LinkedList<PrimitiveTask<O>> tasks;

    private PrimitiveTask<O> currentTask;

    public TaskPlan(@NotNull O owner, @NotNull LinkedList<PrimitiveTask<O>> tasks) {
        this.owner = owner;
        this.tasks = tasks;
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

    boolean isComplete() {
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

        }
    }

    private void end() {
        if (currentTask != null) currentTask.end(owner);

        tasks.clear();
        currentTask = null;
    }

}
