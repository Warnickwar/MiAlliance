package com.mialliance.registers;

import com.mialliance.MiAlliance;
import com.mialliance.ModRegistries;
import com.mialliance.entity.TamableMindComponentEntity;
import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.kits.Behavior;
import com.mialliance.mind.implementation.agent.EntityAgent;
import com.mialliance.mind.implementation.sensor.BlockOfInterestSensor;
import com.mialliance.mind.implementation.strategy.MineBlockStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unchecked")
public class ModBehaviors {

    public static final Behavior DUMMY = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "dummy"), Behavior.start(MindAgent.class).build());

    public static final Behavior TEST_BEHAVIOR = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID,"test"),
        Behavior.start(EntityAgent.class)
            .withPredicate(ag -> ((EntityAgent<TamableMindComponentEntity>) ag).getOwner().getType() == ModEntities.TEST_ENTITY.get())
            .addSensorEntry("WheatSensor", ag -> {
                return new BlockOfInterestSensor((TamableMindComponentEntity) ag.getOwner(), 10, 5, (state) -> {
                    return state.is(Blocks.WHEAT) && ((CropBlock) state.getBlock()).isMaxAge(state);
                }).onInterestChange(sens -> {
                    if (!sens.getStateOfInterest().isAir()) {
                        ag.finishPlan();
                    }});
            }).addBeliefEntry("WheatNearby", ag -> {
                return new MindBelief.Builder("WheatNearby")
                    .withCondition(() -> ((BlockOfInterestSensor) ag.getSensorView().get("WheatSensor")).hasInterest())
                    .withLocation(() -> Vec3.atCenterOf(((BlockOfInterestSensor) ag.getSensorView().get("WheatSensor")).getPositionOfInterest()))
                    .build();
            }).addBeliefEntry("WheatNotNearby", ag -> {
                return new MindBelief.Builder("WheatNotNearby")
                    .withCondition(() -> !ag.getBeliefView().get("WheatNearby").evaluate())
                    .build();
            })
            .addActionEntry("HarvestWheat", ag -> new MindAction.Builder("HarvestWheat")
                .withStrategy(new MineBlockStrategy((TamableMindComponentEntity) ag.getOwner(), 0.35D, 1.25D, () -> new BlockPos(ag.getBeliefView().get("WheatNearby").getLocation())))
                .addPrecondition(ag.getBeliefView().get("WheatNearby"))
                .addEffect(ag.getBeliefView().get("WheatNotNearby"))
                .build())
            .addGoalEntry("mialliance:harvest_wheat", ag -> new MindGoal.Builder("mialliance:harvest_wheat")
                .withPriority(2F)
                .addDesire(ag.getBeliefView().get("WheatNotNearby"))
                .build())
            .build()
            );

    private static Behavior register(ResourceLocation loc, Behavior behavior) {
        Registry.register(ModRegistries.REGISTRIES.MIND_BEHAVIORS, loc, behavior);
        return behavior;
    }
}
