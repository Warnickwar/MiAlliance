package com.mialliance.entity;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.memory.MemoryManager;
import com.mialliance.mind.base.sensor.MindSensor;
import com.mialliance.mind.implementation.sensor.BlockOfInterestSensor;
import com.mialliance.mind.implementation.sensor.EntityLocationSensor;
import com.mialliance.mind.implementation.strategy.EntityIdleStrategy;
import com.mialliance.mind.implementation.strategy.MeleeAttackEntityStrategy;
import com.mialliance.mind.implementation.strategy.MineBlockStrategy;
import com.mialliance.mind.implementation.strategy.WaterAvoidWanderStrategy;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;

public class TestEntity extends TamableMindComponentEntity {

    // TODO: Make and test a Swim action,
    //  as currently the Agent will not swim upwards and
    //  instead will prefer to drown.

    private MemoryManager memories;

    public TestEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        this.memories = new MemoryManager();
    }

    // DEFAULT INFORMATION

    @Override
    protected void setupSensors(HashMap<String, MindSensor> sensors) {

        sensors.put("TargetSensor", new EntityLocationSensor(this, 15, (ent) -> {
            return ent.getType() == EntityType.PIG && this.getSensing().hasLineOfSight(ent);
        }).onLocationChange((sens) -> {
            if (this.getTarget() != sens.getTarget()) {
                if (sens.isTargetDifferent()) {
                    this.getAgent().finishPlan();
                }
                this.setTarget((LivingEntity) sens.getTarget());
            }
            }));

        sensors.put("MeleeTargetSensor", new EntityLocationSensor(this, 5, (ent) -> ent.getType() == EntityType.PIG)
            .onLocationChange((sens) -> {
                // Update only when Target is in range, as to properly
                if (sens.getTarget() == this.getTarget()) {
                    this.getAgent().finishPlan();
                }
            }));

        sensors.put("WheatSensor", new BlockOfInterestSensor(this, 10, 5, (state) -> {
            return state.is(Blocks.WHEAT) && ((CropBlock) state.getBlock()).isMaxAge(state);
        }).onInterestChange(sens -> {
            if (!sens.getStateOfInterest().isAir()) {
                this.getAgent().finishPlan();
            }}));
    }

    @Override
    protected void setupBeliefs(HashMap<String, MindBelief> beliefs, MindAgent.SensorView sensors) {
        beliefs.put("Nothing", new MindBelief.Builder("Nothing")
            .withCondition(() -> false)
            .build());

        beliefs.put("Moving", new MindBelief.Builder("Moving")
            .withCondition(() -> !this.navigation.isDone())
            .build());

        beliefs.put("Attacking", new MindBelief.Builder("Attacking")
            .withCondition(() -> false)
            .build());

        beliefs.put("HasTarget", new MindBelief.Builder("HasTarget")
            .withCondition(() -> this.getTarget() != null)
            .build());

        beliefs.put("TargetNearby", new MindBelief.Builder("TargetNearby")
            .withCondition(() -> ((EntityLocationSensor) sensors.get("TargetSensor")).isTargetInRange())
            .build());

        beliefs.put("TargetInMeleeRange", new MindBelief.Builder("TargetInMeleeRange")
            .withCondition(() -> ((EntityLocationSensor) sensors.get("MeleeTargetSensor")).isTargetInRange())
            .build());

        beliefs.put("WheatNearby", new MindBelief.Builder("WheatNearby")
            .withCondition(() -> ((BlockOfInterestSensor) sensors.get("WheatSensor")).hasInterest())
            .withLocation(() -> Vec3.atCenterOf(((BlockOfInterestSensor) sensors.get("WheatSensor")).getPositionOfInterest()))
            .build());

        beliefs.put("WheatNotNearby", new MindBelief.Builder("WheatNotNearby")
            .withCondition(() -> !beliefs.get("WheatNearby").evaluate())
            .build());
    }

    @Override
    protected void setupActions(HashSet<MindAction> mindActions, MindAgent.BeliefView beliefs) {
        mindActions.add(new MindAction.Builder("Idle")
            .withStrategy(new EntityIdleStrategy(this, 20, 7 * 20))
            .addEffect(beliefs.get("Nothing"))
            .build());

        mindActions.add(new MindAction.Builder("Wander")
            .withStrategy(new WaterAvoidWanderStrategy(this, 0.5D, false))
            .addEffect(beliefs.get("Moving"))
            .build());

        mindActions.add(new MindAction.Builder("AttackPig_Melee")
            .withStrategy(new MeleeAttackEntityStrategy(this, 0.7D, false))
            .withCost(1.0F)
            .addPrecondition(beliefs.get("TargetNearby"))
            .addEffect(beliefs.get("Attacking"))
            .build());

        mindActions.add(new MindAction.Builder("HarvestWheat")
            .withStrategy(new MineBlockStrategy(this, 0.35D, 1.25D, () -> new BlockPos(beliefs.get("WheatNearby").getLocation())))
            .addPrecondition(beliefs.get("WheatNearby"))
            .addEffect(beliefs.get("WheatNotNearby"))
            .build());
    }

    @Override
    protected void setupGoals(HashSet<MindGoal> goals, MindAgent.BeliefView beliefs) {

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

        goals.add(new MindGoal.Builder("mialliance:harvest_wheat")
            .withPriority(2F)
            .addDesire(beliefs.get("WheatNotNearby"))
            .build());
    }

    @Override
    protected void agentPreTick() {
        this.getLevel().getProfiler().push("mialliance:calculatingAgentTick");
    }

    @Override
    protected void agentPlanningTick() {
        this.getLevel().getProfiler().push("mialliance:creatingAgentPlan");
    }

    @Override
    protected void agentPlanRunTick() {
        this.getLevel().getProfiler().push("mialliance:executingAgentPlan");
    }

    @Override
    protected void agentPlanningEndTick() {
        this.getLevel().getProfiler().pop();
    }

    @Override
    protected void agentPlanRunEndTick() {
        this.getLevel().getProfiler().pop();
    }

    @Override
    protected void agentPostTick() {
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
