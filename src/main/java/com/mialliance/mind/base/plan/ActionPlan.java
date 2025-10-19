package com.mialliance.mind.base.plan;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.goal.MindGoal;

import java.util.LinkedList;

public class ActionPlan {

    private final MindGoal goal;
    private final LinkedList<MindAction> actions;
    private final float totalCost;

    public ActionPlan(MindGoal goal, LinkedList<MindAction> actions, float totalCost) {
        this.goal = goal;
        this.actions = actions;
        this.totalCost = totalCost;
    }

    public MindGoal getGoal() {
        return this.goal;
    }

    public LinkedList<MindAction> getActions() {
        return this.actions;
    }
}
