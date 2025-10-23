package com.mialliance.entity.base;

import com.mialliance.communication.CommListener;
import com.mialliance.components.ComponentManager;
import com.mialliance.components.EntityComponentObject;
import com.mialliance.mind.base.MindGoal;
import com.mialliance.mind.base.MindSensor;
import com.mialliance.mind.base.action.IContextProvider;
import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.EntityMindAgentHolder;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.BeliefFactory;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.kits.PlanContext;
import com.mialliance.mind.base.memory.MemoryManager;
import com.mialliance.mind.implementation.agent.EntityAgent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;

// TODO: Implement
//  use GenericMi for the Ownable
public abstract class AbstractMi extends PathfinderMob implements EntityMindAgentHolder, EntityComponentObject<AbstractMi>, Enemy, IContextProvider, CommListener {

    protected final EntityAgent<AbstractMi> agent;
    protected final ComponentManager components;
    protected final MemoryManager memories;

    protected AbstractMi(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        AbstractMi temp = this;
        this.memories = new MemoryManager();
        this.components = new ComponentManager(this, true);
        this.agent = new EntityAgent<>() {
            @Override
            public AbstractMi getOwner() {
                return temp;
            }

            @Override
            protected void setupSensors(HashMap<String, MindSensor> sensors) {
                temp.setupSensors(sensors);
            }

            @Override
            protected void setupBeliefs(HashMap<String, MindBelief> beliefs) {
                temp.setupBeliefs(new BeliefFactory(this, beliefs), this.getSensorView());
            }

            @Override
            protected void setupActions(HashSet<MindAction> actions) {
                temp.setupActions(actions, this.getBeliefView());
            }

            @Override
            protected void setupGoals(HashSet<MindGoal> goals) {
                temp.setupGoals(goals, this.getBeliefView());
            }

            @Override
            public PlanContext<MindAgent<AbstractMi>> collectContext() {
                return temp.collectContext();
            }

            @Override
            protected void onPreTick() {
                temp.agentPreTick();
            }

            @Override
            protected void onPlanningTick() {
                temp.agentPlanningTick();
            }

            @Override
            protected void onPlanRunTick() {
                temp.agentPlanRunTick();
            }

            @Override
            protected void onPlanningEndTick() {
                temp.agentPlanningEndTick();
            }

            @Override
            protected void onPlanRunEndTick() {
                temp.agentPlanRunEndTick();
            }

            @Override
            protected void onPostTick() {
                temp.agentPostTick();
            }

            @Override
            protected void onPlanFinish() {
                temp.onPlanFinish();
            }
        };
    }

    protected abstract void setupSensors(HashMap<String, MindSensor> sensors);

    protected abstract void setupBeliefs(BeliefFactory beliefs, MindAgent.SensorView sensors);

    protected abstract void setupActions(HashSet<MindAction> mindActions, MindAgent.BeliefView beliefs);

    protected abstract void setupGoals(HashSet<MindGoal> goals, MindAgent.BeliefView beliefs);

    protected void agentPreTick() { this.getLevel().getProfiler().push("mialliance:calculatingAgentTick"); }

    protected void agentPlanningTick() { this.getLevel().getProfiler().push("mialliance:creatingAgentPlan"); }

    protected void agentPlanRunTick() { this.getLevel().getProfiler().push("mialliance:executingAgentPlan"); }

    protected void agentPlanningEndTick() { this.getLevel().getProfiler().pop(); }

    protected void agentPlanRunEndTick() { this.getLevel().getProfiler().pop(); }

    protected void agentPostTick() { this.getLevel().getProfiler().pop(); }

    protected void onPlanFinish() {}

    @Override
    public <T> void injectContext(PlanContext<MindAgent<T>> context) {}

    @SuppressWarnings("unchecked")
    protected PlanContext<MindAgent<AbstractMi>> collectContext() { return (PlanContext<MindAgent<AbstractMi>>) PlanContext.NONE; }

}
