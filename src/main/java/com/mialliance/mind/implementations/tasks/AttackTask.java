package com.mialliance.mind.implementations.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import com.mialliance.mind.base.tasks.TaskState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AttackTask extends PrimitiveTask<EntityMindOwner<Mob>> {

    protected AttackTask(@NotNull String identifier, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions, effects);
    }

    @Override
    public boolean start(EntityMindOwner<Mob> owner) {
        return owner.getEntity().getTarget() != null;
    }

    @Override
    public TaskState tick(EntityMindOwner<Mob> owner) {
        return null;
    }

    @Override
    public void end(EntityMindOwner<Mob> owner) {

    }

}
