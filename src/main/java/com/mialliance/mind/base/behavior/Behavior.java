package com.mialliance.mind.base.behavior;

import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.tasks.BaseTask;
import com.mialliance.mind.base.tasks.identifiers.TaskIdentifier;

import java.util.HashMap;

public class Behavior<O extends MindOwner> {

    private final HashMap<TaskIdentifier, BaseTask<O>> tasks;

    public Behavior() {
        this.tasks = new HashMap<>();
    }

    public void addTask(TaskIdentifier, )
}
