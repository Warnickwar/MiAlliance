package com.mialliance.mind.base.plan;

import com.mialliance.mind.base.MindGoal;
import com.mialliance.mind.base.agent.MindAgent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public interface IPlanner {
    <T> ActionPlan plan(MindAgent<T> agent, HashSet<MindGoal> goals, @Nullable MindGoal recentGoal);

    default <T> ActionPlan plan(MindAgent<T> agent, HashSet<MindGoal> goals) { return plan(agent, goals, null); }
}
