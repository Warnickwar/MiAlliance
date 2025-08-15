package com.mialliance.mind.implementations.sensors;

import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.sensors.BaseSensor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WalkTargetSynchronizationSensor extends BaseSensor<EntityMindOwner<PathfinderMob>> {

    public WalkTargetSynchronizationSensor(@NotNull EntityMindOwner<PathfinderMob> owner) {
        super(owner);
    }

    @Override
    protected void register() {
        PathfinderMob mob = owner.getEntity();
        MoveControl control = mob.getMoveControl();
        Vec3 targetPos = new Vec3(control.getWantedX(), control.getWantedY(), control.getWantedZ());
        owner.getAgent().getMemories().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, (float) control.getSpeedModifier(), 0));
    }

    @Override
    public void onTick() {

        PathfinderMob mob = owner.getEntity();
        MoveControl control = mob.getMoveControl();
        Vec3 targetPos = new Vec3(control.getWantedX(), control.getWantedY(), control.getWantedZ());
        owner.getAgent().getMemories().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, (float) control.getSpeedModifier(), 0));
    }

    @Override
    protected void unregister() {}

}
