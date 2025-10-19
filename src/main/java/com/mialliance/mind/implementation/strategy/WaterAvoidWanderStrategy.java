package com.mialliance.mind.implementation.strategy;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WaterAvoidWanderStrategy extends WanderStrategy {

    public WaterAvoidWanderStrategy(PathfinderMob source, double speedModifier, boolean checkNoAction) {
        super(source, speedModifier, checkNoAction);
    }

    // TODO: Later change 0.001F to a configurable Probability
    @Override
    protected @Nullable Vec3 getPosition() {
        if (this.source.isInWaterOrBubble()) {
            Vec3 vec3 = LandRandomPos.getPos(this.source, 15, 7);
            return vec3 == null ? super.getPosition() : vec3;
        } else {
            return this.source.getRandom().nextFloat() >= 0.001F ? LandRandomPos.getPos(this.source, 10, 7) : super.getPosition();
        }
    }

}
