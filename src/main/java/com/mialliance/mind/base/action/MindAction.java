package com.mialliance.mind.base.action;

import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.strategy.IStrategy;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.function.Supplier;

/**
 * <p>
 *     An Action that an {@link com.mialliance.mind.base.agent.MindAgent MindAgent} can take.
 *     Actions have a {@code cost} associated with them, as well as an {@link IStrategy Strategy} handed to them.
 *     When being considered by an Agent, the Action will return a set of {@link MindBelief Beliefs} which either act as
 *     {@code preconditions}, which need to be true to execute the action, or {@code effects} which are assumed to be true
 *     once the action is completed. <b>This does not mean the effects are necessarily true once completed.</b>
 * </p>
 * <p>
 *     Actions can be created using an {@link MindAction.Builder}.
 * </p>
 * @see com.mialliance.mind.base.agent.MindAgent
 * @since 0.0.1
 * @author Warnickwar
 */
public final class MindAction {

    private final String name;
    private Supplier<Float> cost = () -> 1.0F;

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

        public Builder(@NotNull ResourceLocation name) { action = new MindAction(name.toString()); }

        /**
         * <p>
         *      Assigns this action a Cost, which will determine how the action will be considered.
         *      Lower costs will be considered better than higher costs.
         * </p>
         * <p>
         *     The default cost of any action is <b><u>1.0</u></b>.
         * </p>
         * @param cost The cost which will be returned
         * @return This Builder
         */
        public Builder withCost(float cost) {
            action.cost = () -> cost;
            return this;
        }

        /**
         * <p>
         *      Assigns this action a Cost, which will determine how the action will be considered.
         *      Lower costs will be considered better than higher costs.
         * </p>
         * <p>
         *     Using a Supplier, the Agent can be considered in returning a dynamic cost,
         *     such as if an Action should be less considered if it's not as important to do.
         * </p>
         * <p>
         *     The default cost of any action is <b><u>1.0</u></b>.
         * </p>
         * @param costSupplier The supplier returning the final cost value
         * @return This Builder
         */
        public Builder withCost(@NotNull Supplier<Float> costSupplier) {
            action.cost = costSupplier;
            return this;
        }

        /**
         * <p>
         *     Assigns this action a {@link IStrategy Strategy}, which defines how
         *     this action-once planned-will execute.
         * </p>
         * <p>
         *     Every Action is required to have a Strategy associated with it, and calling
         *     {@link Builder#build() MindAction.Builder#build()} will throw an {@link IllegalStateException} if there is no strategy associated.
         * </p>
         * @param strat The {@link IStrategy strategy} that will execute once this action is considered
         * @return This Builder
         */
        public Builder withStrategy(@NotNull IStrategy strat) {
            action.strategy = strat;
            return this;
        }

        /**
         * <p>
         *     Adds a new {@code precondition} to the Action, which has to be assumed true to execute this action.
         * </p>
         * <p>
         *     This takes the form of a {@link MindBelief Belief} which is considered on the Agent.
         * </p>
         * @param precondition The {@link MindBelief} which has to be true in order for this Action to execute
         * @return This Builder
         */
        public Builder addPrecondition(@NotNull MindBelief precondition) {
            action.preconditions.add(precondition);
            return this;
        }

        /**
         * <p>
         *     Adds a new {@code effect} to the Action, which is assumed to be true after this action is completed.
         * </p>
         * <p>
         *     This takes the form of a {@link MindBelief Belief} which is considered on the Agent.
         * </p>
         * @param effect The {@link MindBelief} which is assumed to be true after execution is complete.
         * @return This Builder
         */
        public Builder addEffect(@NotNull MindBelief effect) {
            action.effects.add(effect);
            return this;
        }

        /**
         * <p>
         *     Completes the Builder, and returns the final {@link MindAction}.
         * </p>
         * @return The completed {@link MindAction} with the contents previously assigned.
         * @throws IllegalStateException if this action does not have a Strategy assigned.
         * @see Builder#withStrategy(IStrategy)
         */
        public MindAction build() {
            if (action.strategy == null) throw new IllegalStateException("[MiAlliance] Cannot have an Action without a Strategy associated with it!");
            return action;
        }
    }
}
