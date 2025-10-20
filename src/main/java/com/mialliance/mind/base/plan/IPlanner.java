package com.mialliance.mind.base.plan;

import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.goal.MindGoal;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public interface IPlanner {
    <T> ActionPlan plan(MindAgent<T> agent, HashSet<MindGoal> goals, @Nullable MindGoal recentGoal);

    default <T> ActionPlan plan(MindAgent<T> agent, HashSet<MindGoal> goals) { return plan(agent, goals, null); }

    default <T> ActionPlan plan(MindAgent<T> agent, HashSet<MindGoal> goals, @Nullable MindGoal recentGoal, ProfilerFiller profiler) {
        profiler.push("mialliance:planning");
        ActionPlan plan = plan(agent, goals, recentGoal);
        profiler.pop();
        return plan;
    }
}
