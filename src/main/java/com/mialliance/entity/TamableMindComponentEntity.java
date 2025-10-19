package com.mialliance.entity;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.EntityMindAgentHolder;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.sensor.MindSensor;
import com.mialliance.mind.implementation.agent.EntityAgent;
import com.mialliance.network.packets.MindEntityS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public abstract class TamableMindComponentEntity extends TamableComponentEntity implements EntityMindAgentHolder {

    public static final EntityDataAccessor<Optional<Component>> DATA_CURRENT_ACTION = SynchedEntityData.defineId(TamableMindComponentEntity.class, EntityDataSerializers.OPTIONAL_COMPONENT);
    public static final EntityDataAccessor<Optional<Component>> DATA_CURRENT_GOAL = SynchedEntityData.defineId(TamableMindComponentEntity.class, EntityDataSerializers.OPTIONAL_COMPONENT);
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
                temp.setupBeliefs(beliefs, this.sensors);
            }

            @Override
            protected void setupActions(HashSet<MindAction> mindActions) {
                temp.setupActions(mindActions, this.beliefs);
            }

            @Override
            protected void setupGoals(HashSet<MindGoal> goals) {
                temp.setupGoals(goals, this.beliefs);
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
            protected void onPostTick() {
                temp.agentPostTick();
            }

            @Override
            protected void onPlanFinish() {
                temp.onPlanFinish();
            }
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CURRENT_ACTION, Optional.empty());
        this.entityData.define(DATA_CURRENT_GOAL, Optional.empty());
    }

    protected abstract void setupSensors(HashMap<String, MindSensor> sensors);

    protected abstract void setupBeliefs(HashMap<String, MindBelief> beliefs, HashMap<String, MindSensor> sensors);

    protected abstract void setupActions(HashSet<MindAction> mindActions, HashMap<String, MindBelief> beliefs);

    protected abstract void setupGoals(HashSet<MindGoal> goals, HashMap<String, MindBelief> beliefs);

    protected void agentPreTick() {}

    protected void agentPostTick() {}

    protected void onPlanFinish() {
        this.navigation.stop();
    }

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
