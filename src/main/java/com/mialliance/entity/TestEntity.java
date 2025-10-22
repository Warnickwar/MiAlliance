package com.mialliance.entity;

import com.mialliance.mind.base.MindGoal;
import com.mialliance.mind.base.MindSensor;
import com.mialliance.mind.base.action.IContextProvider;
import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.BeliefFactory;
import com.mialliance.mind.base.kits.PlanContext;
import com.mialliance.mind.base.memory.MemoryManager;
import com.mialliance.mind.implementation.sensor.EntityLocationSensor;
import com.mialliance.mind.implementation.strategy.ChaseStrategy;
import com.mialliance.mind.implementation.strategy.EntityIdleStrategy;
import com.mialliance.mind.implementation.strategy.MeleeAttackEntityStrategy;
import com.mialliance.mind.implementation.strategy.WaterAvoidWanderStrategy;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TestEntity extends TamableMindComponentEntity implements IContextProvider {

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
        })
            .onLocationChange((sens) -> {
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
                //  have the Agent reevaluate the melee range
                if (sens.getTarget() == this.getTarget()) {
                    this.getAgent().finishPlan();
                }
            }));

    }

    @Override
    protected void setupBeliefs(BeliefFactory factory, MindAgent.SensorView sensors) {

        factory.addBelief("Nothing", () -> false);
        factory.addBelief("Moving", () -> !this.navigation.isDone());
        factory.addBelief("Attacking", () -> false);
        factory.addBelief("HasTarget", () -> this.getTarget() != null);
        factory.addBelief("TargetNearby", () -> ((EntityLocationSensor) sensors.get("TargetSensor")).isTargetInRange());
        factory.addBelief("TargetInMeleeRange", () -> this.getTarget() != null && this.getTarget().position().distanceToSqr(this.position()) < getAttackReachSqr(this, this.getTarget()));

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

        //noinspection DataFlowIssue
        mindActions.add(new MindAction.Builder("Chase_Target")
            .withStrategy(new ChaseStrategy(this, 0.7D, false, () -> getAttackReachSqr(this, this.getTarget())))
            .addPrecondition(beliefs.get("HasTarget"))
            .addEffect(beliefs.get("TargetInMeleeRange"))
            .build());

        mindActions.add(new MindAction.Builder("Attack_Melee")
            .withStrategy(new MeleeAttackEntityStrategy(this, 0.7D, false))
            .addPrecondition(beliefs.get("HasTarget"))
            .addPrecondition(beliefs.get("TargetInMeleeRange"))
            .addEffect(beliefs.get("Attacking"))
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

        goals.add(new MindGoal.Builder("mialliance:attack_target")
            .withPriority(2F)
            .addDesire(beliefs.get("Attacking"))
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
    public <T> void injectContext(PlanContext<MindAgent<T>> context) {}

    @Override
    protected PlanContext<MindAgent<TamableMindComponentEntity>> collectContext() {
        List<IContextProvider> entProviders = this.level.getEntities(this, this.getBoundingBox().inflate(10.0D))
            .stream().filter(ent -> ent instanceof IContextProvider)
            .map(filtered -> (IContextProvider) filtered)
            .toList();

        PlanContext<MindAgent<TamableMindComponentEntity>> ctx = PlanContext.create(this);
        entProviders.forEach(prov -> {
            PlanContext<MindAgent<TamableMindComponentEntity>> provContext = PlanContext.create(this);
            prov.injectContext(provContext);
            ctx.mergeThis(provContext);
        });

        // Handle Blocks and BlockEntities later
        return ctx;
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

    protected static double getAttackReachSqr(LivingEntity origin, LivingEntity target) {
        return (double)(origin.getBbWidth() * 2.0F * origin.getBbWidth() * 2.0F + target.getBbWidth());
    }

    public static AttributeSupplier createDefaultAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.ATTACK_DAMAGE, 4).build();
    }

}
