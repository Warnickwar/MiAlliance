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
public final class Behavior<T extends MindAgent<?>> {

    private final Class<T> minimumAgentClass;
    private final SortedSet<Entry<T, ?>> entries;

    private Predicate<MindAgent<?>> canApply;

    private Behavior(Class<T> minClass) {
        this.minimumAgentClass = minClass;
        this.entries = new TreeSet<>(Behavior::compareEntries);
        this.canApply = this.minimumAgentClass::isInstance;
    }

    public void applyToAgent(T agent) {
        if (canApplyTo(agent)) {
            entries.forEach(entry -> entry.applyToAgent(agent));
        }
    }

    public void removeFromAgent(T agent) {
        if (canApplyTo(agent)) {
            entries.forEach(entry -> entry.removeFromAgent(agent));
        }
    }

    public boolean canApplyTo(MindAgent<?> agent) {
        return this.canApply.test(agent);
    }

    public ResourceLocation getKey(Behavior<?> behavior) {
        return ModRegistries.REGISTRIES.MIND_BEHAVIORS.getKey(behavior);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends MindAgent<?>> Behavior<T> getValue(ResourceLocation loc) {
        return (Behavior<T>) ModRegistries.REGISTRIES.MIND_BEHAVIORS.get(loc);
    }

    public static <T extends MindAgent<?>> Builder<T> start(Class<T> minClass) {
        return new Behavior.Builder<>(minClass);
    }

    private static int compareEntries(Entry<?, ?> ent1, Entry<?, ?> ent2) {
        return Integer.compare(ent1.priority, ent2.priority);
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class Builder<T extends MindAgent<?>> {

        private final Behavior<T> result;

        Builder(Class<T> minClass) {
            result = new Behavior<>(minClass);
        }

        @SuppressWarnings("unchecked")
        public Builder<T> withPredicate(Predicate<T> pred) {
            result.canApply = (agent) -> result.minimumAgentClass.isInstance(agent) && pred.test((T) agent);
            return this;
        }

        public Builder<T> addSensorEntry(String id, Function<T, MindSensor> creator) {
            result.entries.add(new SensorEntry<>(id, creator));
            return this;
        }

        public Builder<T> addBeliefEntry(String id, Function<T, MindBelief> creator) {
            result.entries.add(new BeliefEntry<>(id, creator));
            return this;
        }

        public Builder<T> addActionEntry(String id, Function<T, MindAction> creator) {
            result.entries.add(new ActionEntry<>(id, creator));
            return this;
        }

        public Builder<T> addGoalEntry(String id, Function<T, MindGoal> creator) {
            result.entries.add(new GoalEntry<>(id, creator));
            return this;
        }

        public Behavior<T> build() {
            return this.result;
        }
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
