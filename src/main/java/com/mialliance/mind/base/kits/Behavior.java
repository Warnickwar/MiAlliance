package com.mialliance.mind.base.kits;

import com.mialliance.ModRegistries;
import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mialliance.mind.base.sensor.MindSensor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

// Behaviors will be tracked externally, not upon application from the Behavior itself.

/**
 * <p>
 *      Behaviors are objects which hold references to {@link Function Functions} which return
 *      new {@link MindSensor Sensors}, {@link MindBelief Beliefs}, {@link MindAction Actions}, and
 *      {@link MindGoal Goals}.
 * </p>
 * <p>
 *     When queried with a {@link MindAgent Agent}, either through {@link Behavior#applyToAgent(MindAgent)} or
 *     {@link Behavior#removeFromAgent(MindAgent)}, the Behavior will first check to see if the Agent is a valid
 *     target to apply to (per the rules found in {@link Behavior#canApplyTo(MindAgent)}), and then apply the
 *     changes to the Agent.
 * </p>
 * <p>
 *     Changes are always applied in the order of {@link MindSensor Sensors}, {@link MindBelief Beliefs}, {@link MindAction Actions},
 *     and then {@link MindGoal Goals}, without exception. This allows for Beliefs to access Sensors, and for Actions/Goals to access Beliefs
 *     without throwing {@link NullPointerException NullPointerExceptions}.
 * </p>
 * <p>
 *     If De/Serialization is important, {@link Behavior#getKey(Behavior)} and {@link Behavior#getValue(ResourceLocation)}
 *     can be used to Serialize and Deserialize the Behaviors as necessary.
 * </p>
 * @see MindAgent
 * @see MindSensor
 * @see MindBelief
 * @see MindAction
 * @see MindGoal
 * @since 0.0.1
 * @author Warnickwar
 */
public final class Behavior {

    private final Class<? extends MindAgent<?>> minimumAgentClass;
    private final SortedSet<Entry<MindAgent<?>, ?>> entries;

    private Predicate<MindAgent<?>> canApply;

    private Behavior(Class<? extends MindAgent<?>> minClass) {
        this.minimumAgentClass = minClass;
        this.entries = new TreeSet<>(Behavior::compareEntries);
        this.canApply = (ag) -> isInstance(ag, minimumAgentClass);
    }

    /**
     * <p>
     *      Attempts to apply the Behavior to the {@link MindAgent Agent}.
     *      It will not apply if the Agent fails {@link Behavior#canApplyTo(MindAgent)}.
     * </p>
     * @param agent The Agent to attempt applying the Behavior to.
     * @see Behavior#canApplyTo(MindAgent)
     */
    public void applyToAgent(MindAgent<?> agent) {
        if (canApplyTo(agent)) {
            entries.forEach(entry -> entry.applyToAgent(agent));
        }
    }

    /**
     * <p>
     *      Attempts to remove the Behavior from the {@link MindAgent Agent}.
     *      It will not attempt to remove if the Agent fails {@link Behavior#canApplyTo(MindAgent)}.
     * </p>
     * @param agent The Agent to attempt removing the Behavior from.
     * @see Behavior#canApplyTo(MindAgent)
     */
    public void removeFromAgent(MindAgent<?> agent) {
        if (canApplyTo(agent)) {
            entries.forEach(entry -> entry.removeFromAgent(agent));
        }
    }

    /**
     * <p>
     *     Returns if the {@link MindAgent Agent} is valid to effect by this Behavior.
     * </p>
     * @param agent The {@link MindAgent Agent} to test.
     * @return True if the agent can accept this Behavior, false otherwise.
     */
    public boolean canApplyTo(MindAgent<?> agent) {
        return this.canApply.test(agent);
    }

    /**
     * @param behavior The Behavior to get the {@link ResourceLocation} for.
     * @return The Behavior's corresponding ResourceLocation found in the registry.
     * @see ModRegistries.REGISTRIES#MIND_BEHAVIORS
     */
    public static ResourceLocation getKey(Behavior behavior) {
        return ModRegistries.REGISTRIES.MIND_BEHAVIORS.getKey(behavior);
    }

    /**
     * @param loc The {@link ResourceLocation} to search up a corresponding Behavior for.
     * @return The Behavior found with this ResourceLocation, or null if there is no Behavior registered.
     * @see ModRegistries.REGISTRIES#MIND_BEHAVIORS
     */
    @Nullable
    public static Behavior getValue(ResourceLocation loc) {
        return ModRegistries.REGISTRIES.MIND_BEHAVIORS.get(loc);
    }

    /**
     * <p>
     *     Starts a new Behavior {@link Builder}.
     * </p>
     * @param minClass The minimum class of {@link MindAgent} that can be accepted by this Behavior.
     * @return A new {@link Builder} for this Behavior.
     * @param <T> The {@link MindAgent} minimum required for this Behavior.
     */
    public static <T extends MindAgent<?>> Builder<T> start(Class<T> minClass) {
        return new Behavior.Builder<>(minClass);
    }

    private static int compareEntries(Entry<?, ?> ent1, Entry<?, ?> ent2) {
        int priority = Integer.compare(ent1.priority, ent2.priority);
        // Don't override, place it after
        //  Newest Entry should be after older entries, but not before the next section.
        if (priority == 0) priority = 1;
        return priority;
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class Builder<T extends MindAgent<?>> {

        private final Behavior result;

        Builder(Class<T> minClass) {
            result = new Behavior(minClass);
        }

        /**
         * <p>
         *     Assigns this Behavior a {@link Predicate}, which is used to determine
         *     further conditions before a Behavior's application.
         * </p>
         * @param pred The predicate to evaluate with more conditionals
         * @return This Builder
         */
        @SuppressWarnings("SpellCheckingInspection")
        public Builder<T> withPredicate(Predicate<T> pred) {
            result.canApply = (agent) -> canApply(agent, result.minimumAgentClass, pred);
            return this;
        }

        /**
         * <p>
         *     Adds a new {@link MindSensor Sensor} Entry using a {@link Function} accepting
         *     the corresponding {@link MindAgent} and returning a new Sensor. The Sensor should be
         *     a new instance each time, using the Agent's information.
         * </p>
         * @param id The name of the Sensor to add to the Agent
         * @param creator The function returning a new {@link MindSensor}
         * @return This Builder
         */
        @SuppressWarnings("unchecked")
        public Builder<T> addSensorEntry(String id, Function<T, MindSensor> creator) {
            result.entries.add((Entry<MindAgent<?>, ?>) new SensorEntry<>(id, creator));
            return this;
        }

        /**
         * <p>
         *     Adds a new {@link MindBelief Belief} Entry using a {@link Function} accepting
         *     the corresponding {@link MindAgent} and returning a new Belief. The Belief should be
         *     a new instance each time, using the Agent's information.
         * </p>
         * @param id The name of the Belief to add to the Agent
         * @param creator The function returning a new {@link MindBelief}
         * @return This Builder
         */
        @SuppressWarnings("unchecked")
        public Builder<T> addBeliefEntry(String id, Function<T, MindBelief> creator) {
            result.entries.add((Entry<MindAgent<?>, ?>) new BeliefEntry<>(id, creator));
            return this;
        }

        /**
         * <p>
         *     Adds a new {@link MindAction Action} Entry using a {@link Function} accepting
         *     the corresponding {@link MindAgent} and returning a new Action. The Action should be
         *     a new instance each time, using the Agent's information.
         * </p>
         * @param id The name of the Action to add to the Agent
         * @param creator The function returning a new {@link MindAction}
         * @return This Builder
         */
        @SuppressWarnings("unchecked")
        public Builder<T> addActionEntry(String id, Function<T, MindAction> creator) {
            result.entries.add((Entry<MindAgent<?>, ?>) new ActionEntry<>(id, creator));
            return this;
        }

        /**
         * <p>
         *     Adds a new {@link MindGoal Goal} Entry using a {@link Function} accepting
         *     the corresponding {@link MindAgent} and returning a new Goal. The Goal should be
         *     a new instance each time, using the Agent's information.
         * </p>
         * @param id The name of the Goal to add to the Agent
         * @param creator The function returning a new {@link MindGoal}
         * @return This Builder
         */
        @SuppressWarnings("unchecked")
        public Builder<T> addGoalEntry(String id, Function<T, MindGoal> creator) {
            result.entries.add((Entry<MindAgent<?>, ?>) new GoalEntry<>(id, creator));
            return this;
        }

        /**
         * <p>
         *     Completes the Builder, and returns the final {@link Behavior}.
         * </p>
         * @return The completed {@link Behavior} with the contents previously assigned.
         * @see Behavior#applyToAgent(MindAgent)
         * @see Behavior#removeFromAgent(MindAgent) 
         */
        public Behavior build() {
            return this.result;
        }

    }
    @SuppressWarnings("unchecked")
    private static <T extends MindAgent<?>> boolean canApply(MindAgent<?> toCheck, Class<? extends MindAgent<?>> minClass, Predicate<T> customPred) {
        return isInstance(toCheck, minClass) && customPred.test((T) toCheck);
    }

    private static boolean isInstance(MindAgent<?> toCheck, Class<? extends MindAgent<?>> minClass) {
        return minClass.isInstance(toCheck);
    }

    private static abstract class Entry<T extends MindAgent<?>, V> {

        protected final String name;
        protected final Function<T, V> creator;
        private final int priority;

        Entry(String name, Function<T, V> creator, int priority) {
            this.name = name;
            this.creator = creator;
            this.priority = priority;
        }

        abstract void applyToAgent(T agent);

        abstract void removeFromAgent(T agent);
    }

    private static class SensorEntry<T extends MindAgent<?>> extends Entry<T, MindSensor> {

        SensorEntry(String name, Function<T, MindSensor> creator) {
            super(name, creator, 1);
        }

        @Override
        void applyToAgent(T agent) {
            agent.addSensor(name, creator.apply(agent));
        }

        @Override
        void removeFromAgent(T agent) {
            agent.removeSensor(name);
        }

    }

    private static class BeliefEntry<T extends MindAgent<?>> extends Entry<T, MindBelief> {

        BeliefEntry(String name, Function<T, MindBelief> belief) {
            super(name, belief, 2);
        }

        @Override
        void applyToAgent(T agent) {
            agent.addBelief(creator.apply(agent));
        }

        @Override
        void removeFromAgent(T agent) {
            agent.removeBelief(name);
        }
    }

    private static class ActionEntry<T extends MindAgent<?>> extends Entry<T, MindAction> {

        ActionEntry(String name, Function<T, MindAction> action) {
            super(name, action, 3);
        }

        @Override
        void applyToAgent(T agent) {
            agent.addAction(creator.apply(agent));
        }

        @Override
        void removeFromAgent(T agent) {
            agent.removeAction(name);
        }

    }

    private static class GoalEntry<T extends MindAgent<?>> extends Entry<T, MindGoal> {

        GoalEntry(String name, Function<T, MindGoal> creator) {
            super(name, creator, 4);
        }

        @Override
        void applyToAgent(T agent) {
            agent.addGoal(creator.apply(agent));
        }

        @Override
        void removeFromAgent(T agent) {
            agent.removeGoal(name);
        }

    }
}
