package com.mialliance.mind.implementation.strategy;

import com.mialliance.mind.base.IStrategy;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WanderStrategy implements IStrategy {

    private static final DesiredPos NONE = new DesiredPos(0, -256, 0);

    protected final PathfinderMob source;

    protected final double speedModifier;
    // Use to simplify memory management instead of constantly re/making Vec3s
    protected final DesiredPos desiredPos;

    protected boolean checkNoActionTime;
    private boolean checkLocation;

    public WanderStrategy(PathfinderMob source, double speedModifier, boolean checkNoAction) {
        this.source = source;
        this.speedModifier = speedModifier;
        this.desiredPos = new DesiredPos(NONE.x, NONE.y, NONE.z);
        this.checkNoActionTime = checkNoAction;
        this.checkLocation = true;
    }

    @Override
    public void start() {
        this.checkLocation = false;
        this.source.getNavigation().moveTo(desiredPos.x, desiredPos.y, desiredPos.z, this.speedModifier);
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean canPerform() {
        if (this.source.getNavigation().isStuck()) return false;
        if (this.source.isVehicle()) return false;

        if (this.checkNoActionTime && this.source.getNoActionTime() >= 100) return false;

        // prevent unnecessary checks and generations
        if (checkLocation) {
            Vec3 pos = getPosition();

            if (pos == null) return false;

            desiredPos.x = pos.x;
            desiredPos.y = pos.y;
            desiredPos.z = pos.z;
        }
        return true;
    }

    @Override
    public void stop(boolean successful) {
        this.checkLocation = true;
        this.source.getNavigation().stop();
    }

    @Override
    public boolean isComplete() {
        return this.source.getNavigation().isDone() || this.source.isVehicle();
    }

    protected @Nullable Vec3 getPosition() {
        return DefaultRandomPos.getPos(source, 10, 7);
    }

    protected static class DesiredPos {
        double x, y, z;

        DesiredPos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            DesiredPos that = (DesiredPos) o;
            return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }

    }
}
