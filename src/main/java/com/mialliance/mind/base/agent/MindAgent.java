package com.mialliance.mind.base.agent;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.memory.MemoryManager;
import com.mialliance.mind.base.plan.ActionPlan;
import com.mialliance.mind.base.plan.BasicPlanner;
import com.mialliance.mind.base.plan.IPlanner;
import com.mialliance.mind.base.sensor.MindSensor;
import com.mialliance.threading.JobManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

    private final BeliefView cachedBeliefView;
    private final SensorView cachedSensorView;

    @Nullable
    private Future<ActionPlan> planFuture;

    public MindAgent() {
        this.beliefs = new HashMap<>();
        this.cachedBeliefView = new BeliefView(this.beliefs);
        this.sensors = new HashMap<>();
        this.cachedSensorView = new SensorView(this.sensors);
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

    protected void onPlanningTick() {}

    protected void onPlanRunTick() {}

    public final void tick() {
        onPreTick();
        // Tick all sensors to work properly
        sensors.values().forEach(MindSensor::tick);
        if (currentAction == null) {
            onPlanningTick();
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
            onPlanningEndTick();
        }

        if (plan != null && currentAction != null) {
            onPlanRunTick();
            currentAction.tick();

            if (currentAction.isComplete()) {
                currentAction.stop(true);
                currentAction = null;

                if (plan.getActions().isEmpty()) {
                    lastGoal = currentGoal;
                    finishPlan();
                    this.onPlanFinish();
                }
            }
            onPlanRunEndTick();
        }
        onPostTick();
    }

    protected void onPlanningEndTick() {}

    protected void onPlanRunEndTick() {}

    protected void onPostTick() {}

    final void calculatePlan() {
        if (isPlanning()) {
            assert planFuture != null;
            if (planFuture.isDone()) {
                try {
                    plan = planFuture.get();
                } catch (ExecutionException | InterruptedException ignored) {
                    // Failed Plan, ignore and continue. Remove PlanFuture when done.
                }
            }
            planFuture = null;
        } else {
            float priorityLevel = currentGoal == null ? 0 :
                currentGoal.getPriority();

            HashSet<MindGoal> goalsToEvaluate;

            if (currentGoal == null) {
                goalsToEvaluate = goals.values().stream().filter(g -> g.getPriority() > priorityLevel).collect(Collectors.toCollection(HashSet::new));
            } else {
                goalsToEvaluate = new HashSet<>(goals.values());
            }

            planFuture = JobManager.submitJob(() -> this.planSupplier(this, goalsToEvaluate, lastGoal));
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

    /**
     * An optional method which can be implemented to
     *  allow for the agent to collect actions outside the agent's default
     *  Action list.
     * @return a set of Actions which are combined when planning.
     * @see com.mialliance.mind.base.action.IActionExposer
     */
    public Set<MindAction> collectAvailableActions() { return Set.of(); }

    @Nullable
    public MindAction getCurrentAction() {
        return this.currentAction;
    }

    @Nullable
    public MindGoal getCurrentGoal() {
        return this.currentGoal;
    }

    public void addSensor(String name, MindSensor sensor) {
        this.sensors.put(name, sensor);
    }

    @Nullable
    public MindSensor removeSensor(String name) {
        return this.sensors.remove(name);
    }

    public void addBelief(MindBelief belief) {
        this.beliefs.put(belief.getName(), belief);
    }

    @Nullable
    public MindBelief removeBelief(String name) {
        return this.beliefs.remove(name);
    }

    public void addAction(MindAction action) {
        this.actions.put(action.getName(), action);
        this.finishPlan();
    }

    @Nullable
    public MindAction removeAction(String name) {
        MindAction res = this.actions.remove(name);
        if (res != null) this.finishPlan();
        return res;
    }

    public void addGoal(MindGoal goal) {
        this.goals.put(goal.getName(), goal);
        this.finishPlan();
    }

    @Nullable
    public MindGoal removeGoal(String name) {
        MindGoal res = this.goals.remove(name);
        if (res != null) this.finishPlan();
        return res;
    }

    public BeliefView getBeliefView() { return this.cachedBeliefView; }

    public SensorView getSensorView() { return this.cachedSensorView; }

    @Nullable
    public ActionPlan getCurrentPlan() {
        return this.plan;
    }

    public boolean isPlanning() {
        return this.planFuture != null;
    }

    @NotNull
    protected IPlanner createPlanner() {
        return new BasicPlanner();
    }

    public abstract static class View<T> {
        protected final HashMap<String, T> backedMap;

        View(HashMap<String, T> map) {
            this.backedMap = map;
        }

        public abstract T get(String key);
    }

    public static class SensorView extends View<MindSensor> {

        SensorView(HashMap<String, MindSensor> backed) {
            super(backed);
        }

        @Override
        public MindSensor get(String key) {
            return this.backedMap.get(key);
        }

    }

    public static class BeliefView extends View<MindBelief> {

        BeliefView(HashMap<String, MindBelief> backed) {
            super(backed);
        }

        public MindBelief get(String key) {
            return this.backedMap.get(key);
        }
    }
}
