package com.mialliance.mind.implementations.tasktrees;

import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.builders.*;
import com.mialliance.mind.base.tasks.BaseTask;
import com.mialliance.mind.base.tasks.CompoundState;
import com.mialliance.mind.implementations.tasks.IdleTask;
import com.mialliance.mind.implementations.tasks.RandomWanderTask;
import com.mialliance.registers.ModMemoryModules;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.PathfinderMob;

import java.util.Objects;

@SuppressWarnings("unchecked")
public final class TemplateTrees {

    public static final RootBuilder<EntityMindOwner<PathfinderMob>> IDLE_TREE = new RootBuilder<EntityMindOwner<PathfinderMob>>("idle_tree")
        .setState(CompoundState.FALLBACK)
        .addChild(new CompoundBuilder<EntityMindOwner<PathfinderMob>>("idle")
            .addPrecondition(ModMemoryModules.IDLE_HAS_MOVED, Objects::nonNull)
            .setState(CompoundState.SEQUENTIAL)
            .addChild(new CustomBuilder<>("idling_activity", (id, preconditions, effects) -> new IdleTask(id, 20, 100, preconditions, effects))
                .addEffect(ModMemoryModules.IDLE_HAS_MOVED, null)))
        .addChild(new CompoundBuilder<EntityMindOwner<PathfinderMob>>("wander")
            .addPrecondition(ModMemoryModules.IDLE_HAS_MOVED, Objects::isNull)
            .setState(CompoundState.SEQUENTIAL)
            .addChild(new CustomBuilder<>("wandering_activity", (id, pre, eff) -> new RandomWanderTask(id, 1.0, 20.0, pre, eff))
                .addEffect(ModMemoryModules.IDLE_HAS_MOVED, Unit.INSTANCE))
            .addChild((BehaviorTreeBuilder<? extends EntityMindOwner<PathfinderMob>, ? extends BaseTask<EntityMindOwner<PathfinderMob>>>) new ModifyStateBuilder("mutate_state")
                .addMemoryChange(ModMemoryModules.IDLE_HAS_MOVED, Unit.INSTANCE)));
}
