package com.mialliance.mind.base.builders;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.MindOwner;
import com.mialliance.mind.base.tasks.BaseTask;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.HashMap;

public abstract class BehaviorTreeBuilder<O extends MindOwner, T extends BaseTask<O>> {

    protected final String identifier;
    protected final HashMap<MemoryModuleType<?>, NullablePredicate<?>> preconditions;

    BehaviorTreeBuilder(String identifier) {
        this.identifier = identifier;
        this.preconditions = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected static <V, O extends MindOwner> void addPrecondition(BehaviorTreeBuilder<O, ?> builder, MemoryModuleType<V> type, NullablePredicate<V> valueCheck) {
        // This is such a disgusting hack, but necessary. It won't add otherwise.
        builder.preconditions.put(type, valueCheck);
    }

    abstract T build();


}
