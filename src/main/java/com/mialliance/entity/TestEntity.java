package com.mialliance.entity;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.memory.MemoryManager;
import com.mialliance.mind.base.sensor.EntityLocationSensor;
import com.mialliance.mind.base.sensor.MindSensor;
import com.mialliance.mind.implementation.strategy.EntityIdleStrategy;
import com.mialliance.mind.implementation.strategy.MeleeAttackEntityStrategy;
import com.mialliance.mind.implementation.strategy.WaterAvoidWanderStrategy;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class TestEntity extends TamableMindComponentEntity {

    private MemoryManager memories;

    public TestEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        this.memories = new MemoryManager();
    }

    // DEFAULT INFORMATION

    @Override
    protected void setupSensors(HashMap<String, MindSensor> sensors) {

        sensors.put("PigInRadiusSensor", new EntityLocationSensor(this, 15, (ent) -> {
            return ent.getType() == EntityType.PIG && this.getSensing().hasLineOfSight(ent);
        }).onLocationChange((sens) -> {
            if (this.getTarget() != sens.getTarget()) {
                if (sens.isTargetDifferent()) {
                    this.getAgent().finishPlan();
                }
                this.setTarget((LivingEntity) sens.getTarget());
            }
            }));

        sensors.put("PigInMeleeSensor", new EntityLocationSensor(this, 5, (ent) -> ent.getType() == EntityType.PIG)
            .onLocationChange((sens) -> this.getAgent().finishPlan()));
    }

    @Override
    protected void setupBeliefs(HashMap<String, MindBelief> beliefs, HashMap<String, MindSensor> sensors) {
        beliefs.put("Nothing", new MindBelief.Builder("Nothing")
            .withCondition(() -> false)
            .build());

        beliefs.put("Moving", new MindBelief.Builder("Moving")
            .withCondition(() -> !this.navigation.isDone())
            .build());

        beliefs.put("Attacking", new MindBelief.Builder("Attacking")
            .withCondition(() -> false)
            .build());

        beliefs.put("PigInRange", new MindBelief.Builder("PigInRange")
            .withCondition(() -> ((EntityLocationSensor) sensors.get("PigInRadiusSensor")).isTargetInRange())
            .build());

        beliefs.put("PigInMelee", new MindBelief.Builder("PigInMelee")
            .withCondition(() -> ((EntityLocationSensor) sensors.get("PigInMeleeSensor")).isTargetInRange())
            .build());
    }

    @Override
    protected void setupActions(HashSet<MindAction> mindActions, HashMap<String, MindBelief> beliefs) {
        mindActions.add(new MindAction.Builder("IdleAction")
            .withStrategy(new EntityIdleStrategy(this, 20, 7 * 20))
            .addEffect(beliefs.get("Nothing"))
            .build());

        mindActions.add(new MindAction.Builder("Wander")
            .withStrategy(new WaterAvoidWanderStrategy(this, 0.5D, false))
            .addEffect(beliefs.get("Moving"))
            .build());

        mindActions.add(new MindAction.Builder("AttackPig_Melee")
            .withStrategy(new MeleeAttackEntityStrategy(this, 0.7D, false))
            .addPrecondition(beliefs.get("PigInRange"))

            .addEffect(beliefs.get("Attacking"))
            .build());
    }

    @Override
    protected void setupGoals(HashSet<MindGoal> goals, HashMap<String, MindBelief> beliefs) {

        goals.add(new MindGoal.Builder("mialliance:stand_idle")
            .withPriority(1F)
            .addDesire(beliefs.get("Nothing"))
            .build());

        goals.add(new MindGoal.Builder("mialliance:wander_avoid_water")
            .withPriority(1F)
            .addDesire(beliefs.get("Moving"))
            .build());

        goals.add(new MindGoal.Builder("mialliance:attack_pig")
            .withPriority(2F)
            .addDesire(beliefs.get("Attacking"))
            .build());
    }

    @Override
    protected void agentPreTick() {
        this.getLevel().getProfiler().push("mialliance:calculatingAgentTick");
        super.agentPreTick();
    }

    @Override
    protected void agentPostTick() {
        this.entityData.set(DATA_CURRENT_ACTION, Optional.of(this.getAgent().getCurrentAction() == null ?
            Component.empty() :
            Component.literal(this.getAgent().getCurrentAction().getName())));
        this.entityData.set(DATA_CURRENT_GOAL, Optional.of(this.getAgent().getCurrentGoal() == null ?
            Component.empty() :
            Component.literal(this.getAgent().getCurrentGoal().getName())));
        super.agentPostTick();
        this.getLevel().getProfiler().pop();
    }

    @Override
    public boolean save(CompoundTag tag) {
        DataResult<Tag> result = MemoryManager.CODEC.encodeStart(NbtOps.INSTANCE, memories);
        result.get().ifLeft(resTag -> tag.put("Memories", resTag));
        return super.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Memories")) {
            DataResult<MemoryManager> manager = MemoryManager.CODEC.parse(NbtOps.INSTANCE, tag.get("Memories"));
            manager.get().ifLeft(mem -> this.memories = mem);
        }
        super.load(tag);
    }

    public static AttributeSupplier createDefaultAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.ATTACK_DAMAGE, 4).build();
    }

}
