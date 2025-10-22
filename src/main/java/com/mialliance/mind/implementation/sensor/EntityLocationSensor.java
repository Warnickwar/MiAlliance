package com.mialliance.mind.implementation.sensor;

import com.mialliance.MiAllianceConstants;
import com.mialliance.mind.base.MindSensor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityLocationSensor extends MindSensor {

    private final Predicate<Entity> filter;

    private final Entity source;

    private final float range;
    private final List<Consumer<EntityLocationSensor>> listenersToChange;

    @Nullable
    private Entity lastTarget;
    @Nullable
    private Entity target;
    private Vec3 lastKnownPosition;

    public EntityLocationSensor(Entity source, float range) {
        this(source, range, (ent) -> ent instanceof LivingEntity);
    }

    public EntityLocationSensor(Entity source, float range, @NotNull Predicate<Entity> filter) {
        super(5);
        this.source = source;
        this.range = range;
        this.filter = filter;
        this.lastKnownPosition = MiAllianceConstants.NULL_LOCATION;
        this.listenersToChange = new ArrayList<>(5);
    }

    @Override
    protected void onTick() {
        Vec3 agentLoc = source.position();
        List<Entity> entities = source.getLevel().getEntities(null, new AABB(agentLoc, agentLoc).inflate(range));
        Optional<Entity> ordered = entities.stream().filter(ent -> ent.position().closerThan(agentLoc, range))
            .sorted((ent1, ent2) -> {
                double ent1Dist = ent1.position().distanceTo(agentLoc);
                double ent2Dist = ent2.position().distanceTo(agentLoc);
                return Double.compare(ent1Dist, ent2Dist);
            })
            // Filter dead or dying entities, we can't do anything with them and that slows down processing.
            .filter((ent) -> {
                if (ent instanceof LivingEntity lEnt) {
                    return !lEnt.isDeadOrDying();
                }
                return true;
            })
            .filter(filter).findFirst();

        this.updateTarget(ordered.orElse(null));
    }

    public EntityLocationSensor onLocationChange(Consumer<EntityLocationSensor> run) {
        this.listenersToChange.add(run);
        return this;
    }

    public void removeListener(Consumer<EntityLocationSensor> run) {
        this.listenersToChange.remove(run);
    }

    public Vec3 getTargetPosition() {
        return this.target == null ? MiAllianceConstants.NULL_LOCATION : this.target.position();
    }

    @Nullable
    public Entity getTarget() {
        return this.target;
    }

    public boolean isTargetInRange() {
        return getTargetPosition() != MiAllianceConstants.NULL_LOCATION;
    }

    public boolean isTargetDifferent() { return this.target != this.lastTarget; }

    private void updateTarget(@Nullable Entity ent) {
        this.lastTarget = this.target;
        this.target = ent;
        Vec3 newPos = getTargetPosition();
        if (isTargetInRange() && (lastKnownPosition != newPos || lastKnownPosition != MiAllianceConstants.NULL_LOCATION)) {
            lastKnownPosition = newPos;
            listenersToChange.forEach(cons -> cons.accept(this));
        }
    }

}
