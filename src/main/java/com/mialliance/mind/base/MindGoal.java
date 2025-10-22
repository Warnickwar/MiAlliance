package com.mialliance.mind.base;

import com.mialliance.mind.base.belief.MindBelief;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class MindGoal {

    private final String name;
    private float priority = 1.0F;

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

        public Builder(@NotNull String name) {
            this.goal = new MindGoal(name);
        }

        public Builder(@NotNull ResourceLocation name) { this.goal = new MindGoal(name.toString()); }

        /**
         * <p>
         *     Assigns this goal a {@code priority}, which is how important it is to this agent that this
         *     goal's effects will evaluate {@code true}. Higher priorities will be considered more than lower
         *     priorities.
         * </p>
         * <p>
         *     The default priority of any given goal is <b><u>1.0</u></b>.
         * </p>
         * @param priority The priority of importance for this goal
         * @return This Builder
         */
        public Builder withPriority(float priority) {
            goal.priority = priority;
            return this;
        }

        /**
         * <p>
         *      Adds a {@link MindBelief Belief} to this goal, in which the goal desires that the Belief become true.
         *      Any number of Beliefs can be added, and a goal is only considered {@code satisfied} when all of them become
         *      true.
         * </p>
         * <p>
         *     <u>Goals without any desires will never become true, nor will execute.</u>
         * </p>
         * @param belief The {@link MindBelief Belief} which is desired to be true
         * @return This Builder
         * @see MindBelief
         */
        public Builder addDesire(@NotNull MindBelief belief) {
            goal.effects.add(belief);
            return this;
        }

        /**
         * <p>
         *     Completes the Builder, and returns the final {@link MindGoal}.
         * </p>
         * @return The completed {@link MindGoal} with the contents previously assigned.
         * @see MindGoal
         */
        public MindGoal build() {
            return goal;
        }
    }

}
