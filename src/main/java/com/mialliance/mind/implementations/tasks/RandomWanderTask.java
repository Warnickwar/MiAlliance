package com.mialliance.mind.implementations.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import com.mialliance.mind.base.tasks.TaskState;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RandomWanderTask extends PrimitiveTask<EntityMindOwner<PathfinderMob>>  {

    public final double speedModifier;
    public final double maxDistance;

    public RandomWanderTask(@NotNull String identifier, double speedModifier, double maxDistance, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions, effects);
        this.speedModifier = speedModifier;
        this.maxDistance = maxDistance;
    }

    @Override
    public boolean start(EntityMindOwner<PathfinderMob> owner) {
        Vec3 destinationPos = DefaultRandomPos.getPos(owner.getEntity(), 10, 7);
        if (destinationPos != null) {
            owner.getEntity().getNavigation().moveTo(destinationPos.x, destinationPos.y, destinationPos.z, this.speedModifier);
            return true;
        }
        return false;
    }

    @Override
    public TaskState tick(EntityMindOwner<PathfinderMob> owner) {
        return owner.getEntity().isVehicle() ? TaskState.FAILURE : owner.getEntity().getNavigation().isDone() ? TaskState.SUCCESS : TaskState.PROCESSING;
    }

    @Override
    public void end(EntityMindOwner<PathfinderMob> owner) {
        owner.getEntity().getNavigation().stop();
    }

}
