package com.mialliance.mind.implementations.sensors;

import com.mialliance.mind.base.agents.EntityMindOwner;
import com.mialliance.mind.base.sensors.BaseSensor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

public class AttackTargetSynchronizationSensor extends BaseSensor<EntityMindOwner<Mob>> {

    protected AttackTargetSynchronizationSensor(@NotNull EntityMindOwner<Mob> owner) {
        super(owner);
    }

    @Override
    protected void register() {}

    @Override
    public void onTick() {
        if (owner.getEntity().getTarget() != null) {
            owner.getAgent().getMemories().setMemory(MemoryModuleType.ATTACK_TARGET, owner.getEntity().getTarget());
        } else if (owner.getAgent().getMemories().hasMemory(MemoryModuleType.ATTACK_TARGET)) {
            owner.getAgent().getMemories().removeMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    @Override
    protected void unregister() {}

}
