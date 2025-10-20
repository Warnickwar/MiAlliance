package com.mialliance.entity;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.EntityMindAgentHolder;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.sensor.MindSensor;
import com.mialliance.mind.implementation.agent.EntityAgent;
import com.mialliance.network.packets.MindEntityS2CPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class TamableMindComponentEntity extends TamableComponentEntity implements EntityMindAgentHolder {

    private final EntityAgent<TamableMindComponentEntity> agent;

    protected TamableMindComponentEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        // Obtain a reference to the entity object to expose it to the Agent
        TamableMindComponentEntity temp = this;
        this.agent = new EntityAgent<>() {
            @Override
            protected void setupSensors(HashMap<String, MindSensor> sensors) {
                temp.setupSensors(sensors);
            }

            @Override
            protected void setupBeliefs(HashMap<String, MindBelief> beliefs) {
                temp.setupBeliefs(beliefs, this.getSensorView());
            }

            @Override
            protected void setupActions(HashSet<MindAction> mindActions) {
                temp.setupActions(mindActions, this.getBeliefView());
            }

            @Override
            protected void setupGoals(HashSet<MindGoal> goals) {
                temp.setupGoals(goals, this.getBeliefView());
            }

            // Weird Hack, but okay.
            //  Still accomplishes the same goal.
            @Override
            public TamableMindComponentEntity getOwner() {
                return temp;
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

            @Override
            public Set<MindAction> collectAvailableActions() {
                return temp.collectNearbyActions();
            }
        };
    }

    protected abstract void setupSensors(HashMap<String, MindSensor> sensors);

    protected abstract void setupBeliefs(HashMap<String, MindBelief> beliefs, MindAgent.SensorView sensors);

    protected abstract void setupActions(HashSet<MindAction> mindActions, MindAgent.BeliefView beliefs);

    protected abstract void setupGoals(HashSet<MindGoal> goals, MindAgent.BeliefView beliefs);

    protected void agentPreTick() {}

    protected void agentPlanningTick() {}

    protected void agentPlanRunTick() {}

    protected void agentPlanningEndTick() {}

    protected void agentPlanRunEndTick() {}

    protected void agentPostTick() {}

    protected void onPlanFinish() {
        this.navigation.stop();
    }

    protected Set<MindAction> collectNearbyActions() { return Set.of(); }

    public EntityAgent<TamableMindComponentEntity> getAgent() {
        return this.agent;
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && !this.isNoAi()) {
            this.agent.tick();
        }
        super.tick();
    }

    @Override
    protected void sendDebugPackets() {
        MindEntityS2CPacket.sendMindInfo(this);
    }

}
