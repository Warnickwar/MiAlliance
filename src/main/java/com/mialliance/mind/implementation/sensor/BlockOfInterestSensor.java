package com.mialliance.mind.implementation.sensor;

import com.mialliance.Constants;
import com.mialliance.mind.base.MindSensor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockOfInterestSensor extends MindSensor {

    private final Predicate<BlockState> filter;

    private final Entity source;

    private final int width;
    private final int height;
    private final List<Consumer<BlockOfInterestSensor>> listenersToChange;

    private final BlockPos.MutableBlockPos positionOfInterest;
    private BlockState stateOfInterest;

    public BlockOfInterestSensor(Entity source, int width, int height, @NotNull Predicate<BlockState> filter) {
        super(5 * 20);
        this.source = source;
        this.width = width;
        this.height = height;
        this.filter = filter;
        this.positionOfInterest = Constants.NULL_BLOCKPOS.mutable();
        this.stateOfInterest = Blocks.AIR.defaultBlockState();
        this.listenersToChange = new ArrayList<>(5);
    }

    @Override
    protected void onTick() {
        BlockPos currentEntityPos = source.getOnPos();
        AABB areaToCheck = new AABB(currentEntityPos.offset(-width, -height, -width), currentEntityPos.offset(width, height, width));
        Level entLevel = source.getLevel();
        BlockPos.MutableBlockPos closestPos = Constants.NULL_BLOCKPOS.mutable();
        BlockState currentDetectedState = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos((int) areaToCheck.minX, (int) areaToCheck.minY, (int) areaToCheck.minZ);
        double currentDistance = Double.MAX_VALUE;
        for (int x = (int) areaToCheck.minX; x < areaToCheck.maxX; x++) {
            for (int y = (int) areaToCheck.minY; y < areaToCheck.maxY; y++) {
                for (int z = (int) areaToCheck.minZ; z < areaToCheck.maxZ; z++) {
                    // Don't bother checking for blocks further away than what we already know exists closer
                    double curDist;
                    if (currentDistance < (curDist = currentPos.distSqr(currentEntityPos))) continue;
                    BlockState currentState = entLevel.getBlockState(currentPos);
                    if (!currentState.isAir() && filter.test(currentState)) {
                        closestPos.set(currentPos);
                        currentDetectedState = currentState;
                        currentDistance = curDist;
                    }
                    currentPos.move(0, 0, 1);
                }
                currentPos.setZ((int) areaToCheck.minZ);
                currentPos.move(0, 1, 0);
            }
            currentPos.setY((int) areaToCheck.minY);
            currentPos.move(1, 0, 0);
        }

        if (!positionOfInterest.equals(closestPos)) {
            stateOfInterest = currentDetectedState;
            positionOfInterest.set(closestPos);
            listenersToChange.forEach(cons -> cons.accept(this));
        } else {
            stateOfInterest = currentDetectedState;
            positionOfInterest.set(closestPos);
        }
    }

    public BlockPos getPositionOfInterest() {
        return this.positionOfInterest.immutable();
    }

    public BlockState getStateOfInterest() {
        return this.stateOfInterest;
    }

    public BlockOfInterestSensor onInterestChange(Consumer<BlockOfInterestSensor> run) {
        this.listenersToChange.add(run);
        return this;
    }

    public void removeListener(Consumer<BlockOfInterestSensor> run) {
        this.listenersToChange.remove(run);
    }

    public boolean hasInterest() {
        return positionOfInterest.getX() != Constants.NULL_BLOCKPOS.getX() &&
            positionOfInterest.getY() != Constants.NULL_BLOCKPOS.getY() &&
            positionOfInterest.getZ() != Constants.NULL_BLOCKPOS.getZ();
    }

}
