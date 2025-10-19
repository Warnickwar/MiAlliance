package com.mialliance.mind.base.goal;

import com.mialliance.mind.base.belief.MindBelief;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class MindGoal {

    private final String name;
    private float priority;

    private final HashSet<MindBelief> effects;

    MindGoal(@NotNull String name) {
        this.name = name;
        this.effects = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public HashSet<MindBelief> getEffects() {
        return effects;
    }

    public float getPriority() {
        return priority;
    }

    public static class Builder {
        private final MindGoal goal;

        public Builder(@NotNull ResourceLocation name) { this.goal = new MindGoal(name.toString()); }

        public Builder(@NotNull String name) {
            this.goal = new MindGoal(name);
        }

        public Builder withPriority(float priority) {
            goal.priority = priority;
            return this;
        }

        public Builder addDesire(@NotNull MindBelief belief) {
            goal.effects.add(belief);
            return this;
        }

        public MindGoal build() {
            return goal;
        }
    }

}
