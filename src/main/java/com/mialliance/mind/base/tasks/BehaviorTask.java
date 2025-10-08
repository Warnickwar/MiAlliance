package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.behavior.Behavior;

public final class BehaviorTask<O extends MindOwner, T extends BaseTask<O>> extends BaseTask<O> {

    private final T task;
    private final Behavior<O> behavior;

    BehaviorTask(T task, Behavior<O> behavior) {
        // It is fine to pass in Null for this precondition- we use the Task's preconditions anyways.
        super(task.getIdentifier(), null);
        this.task = task;
        this.behavior = behavior;
    }

}
