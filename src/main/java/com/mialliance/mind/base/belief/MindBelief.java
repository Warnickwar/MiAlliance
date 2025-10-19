package com.mialliance.mind.base.belief;

import com.mialliance.MiAllianceConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MindBelief {

    private final String name;

    private Supplier<Boolean> condition;
    private Supplier<Vec3> location = () -> MiAllianceConstants.NULL_LOCATION;

    MindBelief(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean evaluate() { return condition.get(); }

    public Vec3 getLocation() { return location.get(); }

    public static class Builder {
        private final MindBelief belief;

        public Builder(@NotNull ResourceLocation name) { this.belief = new MindBelief(name.toString()); }

        public Builder(@NotNull String name) {
            this.belief = new MindBelief(name);
        }

        public Builder withCondition(Supplier<Boolean> condition) {
            belief.condition = condition;
            return this;
        }

        public Builder withLocation(Supplier<Vec3> location) {
            belief.location = location;
            return this;
        }

        public MindBelief build() {
            return belief;
        }
    }

}
