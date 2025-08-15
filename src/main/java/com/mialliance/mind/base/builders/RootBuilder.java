package com.mialliance.mind.base.builders;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.tasks.BaseTask;
import com.mialliance.mind.base.tasks.CompoundState;
import com.mialliance.mind.base.tasks.CompoundTask;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

public class RootBuilder<O extends MindOwner> extends CompoundBuilder<O> {

    public RootBuilder(String identifier) {
        super(identifier);
    }

    @Override
    public @NotNull <V> RootBuilder<O> addPrecondition(MemoryModuleType<V> key, NullablePredicate<V> pre) {
        return (RootBuilder<O>) super.addPrecondition(key, pre);
    }

    @Override
    public @NotNull RootBuilder<O> addChild(BehaviorTreeBuilder<? extends O, ? extends BaseTask<O>> builder) {
        return (RootBuilder<O>) super.addChild(builder);
    }

    @Override
    public @NotNull RootBuilder<O> setState(CompoundState<?> state) {
        return (RootBuilder<O>) super.setState(state);
    }

    public CompoundTask<O> construct() {
        return this.build();
    }

}
