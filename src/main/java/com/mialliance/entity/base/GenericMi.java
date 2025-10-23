package com.mialliance.entity.base;

import com.mialliance.Constants;
import com.mialliance.communication.Communication;
import com.mialliance.components.ComponentManager;
import com.mialliance.mind.base.MindGoal;
import com.mialliance.mind.base.MindSensor;
import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.BeliefFactory;
import com.mialliance.mind.base.kits.Behavior;
import com.mialliance.mind.base.kits.IBehaviorAcceptor;
import com.mialliance.mind.base.kits.IBehaviorRemover;
import com.mialliance.mind.implementation.kits.IBehaviorSlot;
import com.mialliance.registers.ModMemoryModules;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.mialliance.Constants.getMi;

public class GenericMi extends AbstractMi implements OwnableEntity, IBehaviorAcceptor, IBehaviorRemover {

    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(GenericMi.class, EntityDataSerializers.OPTIONAL_UUID);

    protected final Map<String, Behavior> currentBehaviors;

    public GenericMi(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        this.currentBehaviors = new HashMap<>();
    }

    // <---> AGENT SETUP <--->

    @Override
    protected void setupSensors(HashMap<String, MindSensor> sensors) {

    }

    @Override
    protected void setupBeliefs(BeliefFactory beliefs, MindAgent.SensorView sensors) {

        // Generic Beliefs

        beliefs.addBelief("Nothing", () -> false);
        beliefs.addBelief("CanAttack", () -> false);

        // Subordinate Beliefs

        beliefs.addBelief("HasOfficer", () -> this.memories.hasMemory(ModMemoryModules.OFFICER));
        beliefs.addBelief("OfficerLoaded", () -> {
            @SuppressWarnings("DataFlowIssue")
            int officerId = this.memories.getMemory(ModMemoryModules.OFFICER);
            return this.memories.hasMemory(ModMemoryModules.OFFICER) &&
                getMi(officerId, this.level) != null;
        });
        beliefs.addBelief("NearbyOfficer", () -> {
            @SuppressWarnings("DataFlowIssue")
            int officerId = this.memories.getMemory(ModMemoryModules.OFFICER);
            AbstractMi res;
            if (this.memories.hasMemory(ModMemoryModules.OFFICER) && (res = getMi(officerId, this.level)) != null) {
                return this.distanceToSqr(res) <= 5.0D;
            }
            return false;
        });

    }

    @Override
    protected void setupActions(HashSet<MindAction> mindActions, MindAgent.BeliefView beliefs) {

    }

    @Override
    protected void setupGoals(HashSet<MindGoal> goals, MindAgent.BeliefView beliefs) {

        // Idle Goals

        goals.add(new MindGoal.Builder(Constants.LOCATIONS.NOTHING_GOAL)
            .addDesire(beliefs.get("Nothing"))
            .build());

        // Idle Priority Goals

        goals.add(new MindGoal.Builder("FollowOfficer")
            .withPriority(1.5F)
            .addDesire(beliefs.get("HasOfficer"))
            .addDesire(beliefs.get("OfficerLoaded"))
            .addDesire(beliefs.get("NearbyOfficer"))
            .build());
    }

    // <---> BEHAVIOR METHODS <--->

    @Override
    public void accept(Behavior behavior, IBehaviorSlot slot) {
        if (behavior.canApplyTo(this.agent)) {
            behavior.applyToAgent(this.agent);
            this.currentBehaviors.put(slot.getId(), behavior);
        }
    }

    @Override
    public void remove(Behavior behavior) {
        if (behavior.canApplyTo(this.agent)) {
            behavior.removeFromAgent(this.agent);
            this.currentBehaviors.entrySet().removeIf(ent -> ent.getValue() == behavior);
        }
    }

    // <---> INTERFACE METHODS <--->

    @Override
    public AbstractMi getEntity() {
        return this;
    }

    @Override
    public MindAgent<?> getAgent() {
        return this.agent;
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    @Override
    public @Nullable Entity getOwner() {
        try {
            UUID uuid = this.getOwnerUUID();
            return uuid == null ? null : this.level.getPlayerByUUID(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // <---> PUBLIC METHODS <--->

    @Override
    public void onRecieveMessage(@NotNull Communication comm) {
        // TODO: Evaluate intent, check for allied communication, and apply to memories
        //  I'm too lazy so for now I'll just apply to memories.

    }

    @Override
    public ComponentManager getManager() {
        return this.components;
    }

    // <---> PRIVATE METHODS <--->

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        super.defineSynchedData();
    }

}
