package com.mialliance.mind.implementation.strategy;

import com.mialliance.MiAllianceConstants;
import com.mialliance.mind.base.strategy.IStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class MineBlockStrategy implements IStrategy {

    private final PathfinderMob host;
    private final double speedMod;
    private final double distanceToMine;
    private final Supplier<BlockPos> positionProvider;

    private BlockPos currentTarget = MiAllianceConstants.NULL_BLOCKPOS;
    private boolean isComplete = false;

    public MineBlockStrategy(PathfinderMob host, double speedModifier, double distanceToMine, Supplier<BlockPos> positionProvider) {
        this.host = host;
        this.speedMod = speedModifier;
        this.distanceToMine = distanceToMine;
        this.positionProvider = positionProvider;
    }

    @Override
    public void start() {
        BlockPos provided = positionProvider.get();
        if (MiAllianceConstants.NULL_BLOCKPOS.equals(provided)) {
            isComplete = true;
            return;
        }
        currentTarget = provided;
        host.getNavigation().moveTo(currentTarget.getX(), currentTarget.getY()+1, currentTarget.getZ(), speedMod);
        this.isComplete = false;
    }

    @Override
    public void tick() {
        if (!isComplete && host.position().closerThan(Vec3.atCenterOf(currentTarget), distanceToMine) && this.currentTarget != MiAllianceConstants.NULL_BLOCKPOS) {
            host.level.destroyBlock(currentTarget, true, host);
            this.isComplete = true;
        }
    }

    @Override
    public void stop(boolean successful) {
        host.getNavigation().stop();
        this.currentTarget = MiAllianceConstants.NULL_BLOCKPOS;
    }

    @Override
    public boolean canPerform() {
        return currentTarget != MiAllianceConstants.NULL_BLOCKPOS || host.getNavigation().isStuck();
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }



}
