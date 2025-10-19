package com.mialliance.mind.base.action;

import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.strategy.IStrategy;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.function.Supplier;

public final class MindAction {

    private final String name;
    private Supplier<Float> cost = () -> 0.0F;

    private final HashSet<MindBelief> preconditions;
    private final HashSet<MindBelief> effects;

    private boolean forceEnd;

    private IStrategy strategy;

    MindAction(String name) {
        this.name = name;
        this.preconditions = new HashSet<>();
        this.effects = new HashSet<>();
        this.forceEnd = false;
    }

    public String getName() {
        return this.name;
    }

    public float getCost() {
        return cost.get();
    }

    public boolean isComplete() {
        return forceEnd || this.strategy.isComplete();
    }

    public void start() {
        forceEnd = false;
        this.strategy.start();
    }

    public void tick() {
        if (this.strategy.canPerform()) {
            this.strategy.tick();
        } else {
            forceEnd = true;
        }
    }

    public void stop(boolean successful) {
        this.strategy.stop(successful);
    }

    public HashSet<MindBelief> getPreconditions() {
        return this.preconditions;
    }

    public HashSet<MindBelief> getEffects() {
        return this.effects;
    }

    public static class Builder {
        private final MindAction action;

        public Builder(@NotNull String name) {
            action = new MindAction(name);
        }

        public Builder withCost(float cost) {
            action.cost = () -> cost;
            return this;
        }

        public Builder withCost(@NotNull Supplier<Float> costSupplier) {
            action.cost = costSupplier;
            return this;
        }

        public Builder withStrategy(@NotNull IStrategy strat) {
            action.strategy = strat;
            return this;
        }

        public Builder addPrecondition(@NotNull MindBelief precondition) {
            action.preconditions.add(precondition);
            return this;
        }

        public Builder addEffect(@NotNull MindBelief effect) {
            action.effects.add(effect);
            return this;
        }

        public MindAction build() {
            return action;
        }
    }
}
