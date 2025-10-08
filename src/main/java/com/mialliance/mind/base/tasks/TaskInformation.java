package com.mialliance.mind.base.tasks;

import com.mialliance.mind.base.agents.MindOwner;

public record TaskInformation<O extends MindOwner>(TaskPriority priority, BaseTask<O> task) {}
