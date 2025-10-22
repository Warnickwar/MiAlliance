package com.mialliance.mind.base.belief;

import com.mialliance.MiAllianceConstants;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.MindGoal;
import com.mialliance.mind.base.MindSensor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * <p>
 *     A MindBelief represents an {@link MindAgent Agent's} beliefs, or thoughts, about their environment.
 *     This is used by {@link MindGoal goals} to dictate which goals should be prioritized, or done.
 * </p>
 * <p>
 *     Evaluating {@code true} means that this belief, and all its conditions, are satisfied.
 * </p>
 * <p>
 *     Evaluating {@code false} means that this belief is not satisfied, and something is wrong.
 * </p>
 * <p>
 *     Beliefs can be chained together with {@link MindSensor Sensors} to detect conditions about the
 *     environment, and evaluate conditions which indicate things about the state of the world or agent.
 * </p>
 * <p>
 *     Beliefs can also, optionally, supply an {@link Vec3} as a {@code Location} to be used by actions. By default,
 *     the Belief will supply a location of {@link MiAllianceConstants#NULL_LOCATION}, which indicates a location
 *     that is not set. This is different from a position of {@code (0,0,0)}, as to avoid collisions with (0,0,0) in the
 *     actual world.
 * </p>
 * <p>
 *     By default, Beliefs will return a conditional of {@code true} (always satisfied), and
 *     a location of {@link MiAllianceConstants#NULL_LOCATION} (no or invalid location) unless otherwise set.
 * </p>
 * @see MindAgent
 * @see MindGoal
 * @see MindSensor
 * @since 0.0.1
 * @author Warnickwar
 */
public class MindBelief {

    private final String name;

    private Supplier<Boolean> condition = () -> true;
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

        /**
         * <p>
         *     Assigns this belief a Conditional, which will determine if this
         *     belief is satisfied or not.
         * </p>
         * @param condition The conditional to evaluate on whether the belief is satisfied or not
         * @return This Builder
         */
        public Builder withCondition(Supplier<Boolean> condition) {
            belief.condition = condition;
            return this;
        }

        /**
         * <p>
         *     Assigns this belief a Location Supplier, which will return a {@link Vec3}
         *     which can be used anywhere. Most commonly, this is queried in the case of this belief evaluating
         *     {@code false}.
         * </p>
         * <p>
         *     Any location suppliers should, by default, return {@link MiAllianceConstants#NULL_LOCATION}
         *     to indicate an invalid or empty location, and should <u>never</u> return {@code null}.
         * </p>
         * @param location The location supplier which returns a {@link Vec3} location.
         * @return
         */
        public Builder withLocation(Supplier<Vec3> location) {
            belief.location = location;
            return this;
        }

        /**
         * <p>
         *     Completes the Builder, and returns the final {@link MindBelief}.
         * </p>
         * @return The completed {@link MindBelief} with the contents previously assigned.
         */
        public MindBelief build() {
            return belief;
        }
    }

}
