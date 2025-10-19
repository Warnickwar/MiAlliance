package com.mialliance.mind.base.agent;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.memory.MemoryManager;
import com.mialliance.mind.base.plan.ActionPlan;
import com.mialliance.mind.base.plan.BasicPlanner;
import com.mialliance.mind.base.plan.IPlanner;
import com.mialliance.mind.base.sensor.MindSensor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public abstract class MindAgent<T> {

    @Nullable
    protected MindGoal lastGoal;
    @Nullable
    protected ActionPlan plan;
    @Nullable
    protected MindGoal currentGoal;
    @Nullable
    protected MindAction currentAction;

    protected final MemoryManager memories;

    protected final HashMap<String, MindBelief> beliefs;
    protected final HashMap<String, MindSensor> sensors;
    protected final HashMap<String, MindAction> actions;
    protected final HashMap<String, MindGoal> goals;

    protected final IPlanner planner;

    public MindAgent() {
        this.beliefs = new HashMap<>();
        this.sensors = new HashMap<>();
        this.actions = new HashMap<>();
        HashSet<MindAction> tempActions = new HashSet<>();
        this.goals = new HashMap<>();
        HashSet<MindGoal> tempGoals = new HashSet<>();
        this.memories = new MemoryManager();
        setupSensors(this.sensors);
        setupBeliefs(this.beliefs);
        setupActions(tempActions);
        tempActions.forEach(act -> this.actions.put(act.getName(), act));
        setupGoals(tempGoals);
        tempGoals.forEach(goal -> this.goals.put(goal.getName(), goal));
        this.planner = createPlanner();
    }

    public abstract T getOwner();

    public HashSet<MindAction> getActions() {
        return new HashSet<>(this.actions.values());
    }

    // Exposed to allow for new goals and actions to be added
    //  While using the current agent's belief system
    public HashMap<String, MindBelief> getBeliefs() {
        return this.beliefs;
    }

    protected void onPreTick() {}

    public final void tick() {
        onPreTick();
        // Tick all sensors to work properly
        sensors.values().forEach(MindSensor::tick);
        if (currentAction == null) {
            calculatePlan();
            if (plan != null && !plan.getActions().isEmpty()) {

                this.currentGoal = plan.getGoal();
                this.currentAction = plan.getActions().pop();
                if (currentAction.getPreconditions().stream().allMatch(MindBelief::evaluate)) {
                    this.currentAction.start();
                } else {
                    finishPlan();
                    this.onPlanFinish();
                }
            }
        }

        if (plan != null && currentAction != null) {
            currentAction.tick();

            if (currentAction.isComplete()) {
                currentAction.stop(false);
                currentAction = null;

                if (plan.getActions().isEmpty()) {
                    lastGoal = currentGoal;
                    finishPlan();
                    this.onPlanFinish();
                }
            }
        }
        onPostTick();
    }

    protected void onPostTick() {}


    final void calculatePlan() {
        float priorityLevel = currentGoal == null ? 0 :
            currentGoal.getPriority();

        HashSet<MindGoal> goalsToEvaluate = new HashSet<>(goals.values());

        if (currentGoal == null) {
            goalsToEvaluate = goals.values().stream().filter(g -> g.getPriority() > priorityLevel).collect(Collectors.toCollection(HashSet::new));
        }

        ActionPlan plan = planner.plan(this, goalsToEvaluate, lastGoal);
        if (plan != null) {
            this.plan = plan;
        }
    }

    protected ActionPlan planSupplier(MindAgent<T> agent, HashSet<MindGoal> goals, @Nullable MindGoal lastGoal) {
        return planner.plan(agent, goals, lastGoal);
    }

    public void finishPlan() {
        if (currentAction != null) {
            this.currentAction.stop(false);
            this.currentAction = null;
        }
        plan = null;
        lastGoal = currentGoal;
        currentGoal = null;
    }

    public abstract Vec3 getLocation();

    public abstract Level getLevel();

    protected abstract void setupSensors(HashMap<String, MindSensor> sensors);

    protected abstract void setupBeliefs(HashMap<String, MindBelief> beliefs);

    protected abstract void setupActions(HashSet<MindAction> actions);

    protected abstract void setupGoals(HashSet<MindGoal> goals);

    protected abstract void onPlanFinish();

    @Nullable
    public MindAction getCurrentAction() {
        return this.currentAction;
    }

    @Nullable
    public MindGoal getCurrentGoal() {
        return this.currentGoal;
    }

    public void addGoal(MindGoal goal) {
        this.goals.put(goal.getName(), goal);
    }

    @Nullable
    public MindGoal removeGoal(String name) {
        return this.goals.remove(name);
    }

    public void addAction(MindAction action) {
        this.actions.put(action.getName(), action);
    }

    @Nullable
    public MindAction removeAction(String name) {
        return this.actions.remove(name);
    }

    public void addBelief(MindBelief belief) {
        this.beliefs.put(belief.getName(), belief);
    }

    @Nullable
    public MindBelief removeBelief(String name) {
        return this.beliefs.remove(name);
    }

    @Nullable
    public ActionPlan getCurrentPlan() {
        return this.plan;
    }

    @NotNull
    protected IPlanner createPlanner() {
        return new BasicPlanner();
    }
}
