package com.mialliance.mind.base.action;

import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.strategy.IStrategy;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class MindAction {

    private final String name;
    private float cost = 0.0F;

    private final HashSet<MindBelief> preconditions;
    private final HashSet<MindBelief> effects;

    private IStrategy strategy;

    MindAction(String name) {
        this.name = name;
        this.preconditions = new HashSet<>();
        this.effects = new HashSet<>();
    }

    public String getName() {
        return this.name;
    }

    public float getCost() {
        return cost;
    }

    public boolean isComplete() {
        return this.strategy.isComplete();
    }

    public void start() {
        this.strategy.start();
    }

    public void tick() {
        if (this.strategy.canPerform()) {
            this.strategy.tick();
        }

        if (!strategy.isComplete()) return;

        effects.forEach(MindBelief::evaluate);
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
            action.cost = cost;
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
