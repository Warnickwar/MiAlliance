package com.mialliance.mind.implementations.tasks;

import com.mialliance.mind.base.NullablePredicate;
import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.memories.TemplateValue;
import com.mialliance.mind.base.tasks.PrimitiveTask;
import com.mialliance.mind.base.tasks.TaskState;
import com.mialliance.registers.ModMemoryModules;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

public class LungeAttackTask extends PrimitiveTask<EntityMindOwner<PathfinderMob>> {

    private static final Consumer<PathfinderMob> EMPTY_ONLEAP = (mob) -> {};

    private final long lungeCooldown;
    private final float yDelta;
    private final Vec3 distanceMultiplier;
    private final Consumer<PathfinderMob> onLeap;

    public LungeAttackTask(@NotNull String identifier, long lungeCooldown, float yDelta, @NotNull Vec3 distanceMultiplier, Consumer<PathfinderMob> onLeap, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        super(identifier, preconditions, effects);
        this.lungeCooldown = lungeCooldown;
        this.yDelta = yDelta;
        this.distanceMultiplier = distanceMultiplier;
        this.onLeap = onLeap;
    }

    public LungeAttackTask(@NotNull String identifier, long lungeCooldown, float yDelta, @NotNull Vec3 distanceMultiplier, @NotNull Map<MemoryModuleType<?>, NullablePredicate<?>> preconditions, @NotNull Map<MemoryModuleType<?>, TemplateValue<?>> effects) {
        this(identifier, lungeCooldown, yDelta, distanceMultiplier, EMPTY_ONLEAP, preconditions, effects);
    }

    @Override
    public boolean start(EntityMindOwner<PathfinderMob> owner) {
        if (owner.getEntity().getTarget() == null) return false;
        PathfinderMob mob = owner.getEntity();
        LivingEntity target = mob.getTarget();
        Vec3 currentMovement = mob.getDeltaMovement();
        Vec3 lungeDirection = new Vec3(target.getX()-mob.getX(), 0.0D, target.getZ()-mob.getZ());
        if (lungeDirection.lengthSqr() > 1.0E-7D) {
            lungeDirection = lungeDirection.normalize().scale(0.6D).add(currentMovement.scale(0.3D));
        }

        float yStrength = yDelta;
        if (!mob.hasEffect(MobEffects.DAMAGE_RESISTANCE) && mob.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            lungeDirection = lungeDirection.multiply(0.5D, 0.5D, 0.5D);
            yStrength /= 2;
        }

        lungeDirection = lungeDirection.multiply(distanceMultiplier);

        mob.setDeltaMovement(lungeDirection.x, yStrength, lungeDirection.z);
        this.onLeap.accept(mob);
        return true;
    }

    @Override
    public TaskState tick(EntityMindOwner<PathfinderMob> owner) {
        return TaskState.SUCCESS;
    }

    @Override
    public void end(EntityMindOwner<PathfinderMob> owner) {
        owner.getAgent().getMemories().setMemory(ModMemoryModules.LUNGE_COOLDOWN, Unit.INSTANCE, lungeCooldown);
    }

}
