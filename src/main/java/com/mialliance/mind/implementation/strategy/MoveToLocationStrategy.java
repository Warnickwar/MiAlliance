package com.mialliance.mind.implementation.strategy;

import com.mialliance.mind.base.IStrategy;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class MoveToLocationStrategy implements IStrategy {

    protected final PathfinderMob host;
    protected final double speedModifier;
    protected final boolean checkNoAction;
    private final Supplier<Vec3> locationSupplier;

    public MoveToLocationStrategy(PathfinderMob host, double speedModifier, boolean checkNoAction, Supplier<Vec3> locationSupplier) {
        this.host = host;
        this.speedModifier = speedModifier;
        this.checkNoAction = checkNoAction;
        this.locationSupplier = locationSupplier;
    }

    @Override
    public void start() {
        Vec3 target = locationSupplier.get();
        this.host.getNavigation().moveTo(target.x, target.y, target.z, speedModifier);
    }

    @Override
    public void tick() {}

    @Override
    public void stop(boolean successful) {
        this.host.getNavigation().stop();
    }

    @Override
    public boolean canPerform() {
        if (checkNoAction && this.host.getNoActionTime() >= 100) return false;
        return !this.host.getNavigation().isStuck() || !this.host.isVehicle();
    }

    @Override
    public boolean isComplete() {
        return this.host.getNavigation().isDone() || this.host.isVehicle();
    }

}
