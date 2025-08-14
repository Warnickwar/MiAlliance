package com.mialliance.mind.base.builders;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.tasks.CompoundState;
import com.mialliance.mind.base.tasks.CompoundTask;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class CompoundBuilder<O extends MindOwner> extends BehaviorTreeBuilder<O, CompoundTask<O>> {
    protected CompoundState<?> state;

    // Used solely in order to retain the children ordering-important in AI evaluation
    protected LinkedList<BehaviorTreeBuilder<O, ?>> children;

    public CompoundBuilder(String identifier) {
        super(identifier);
        this.state = CompoundState.FALLBACK;
        this.children = new LinkedList<>();
    }

    @NotNull
    public <V> CompoundBuilder<O> addPrecondition(MemoryModuleType<V> key, NullablePredicate<V> pre) {
        addPrecondition(this, key, pre);
        return this;
    }

    @NotNull
    public CompoundBuilder<O> setState(CompoundState<?> state) {
        this.state = state;
        return this;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public CompoundBuilder<O> addChild(BehaviorTreeBuilder<? extends O, ?> builder) {
        this.children.add((BehaviorTreeBuilder<O, ?>) builder);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    CompoundTask<O> build() {
        return new CompoundTask<>(identifier, (CompoundState<O>) state, preconditions, new LinkedList<>(children.stream().map(BehaviorTreeBuilder::build).toList()));
    }

}