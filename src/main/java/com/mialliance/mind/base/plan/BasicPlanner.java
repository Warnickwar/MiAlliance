package com.mialliance.mind.base.plan;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.goal.MindGoal;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class BasicPlanner implements IPlanner {

    private static final Logger LOGGER = LogUtils.getLogger();

    // TODO: Make Planner evaluate multiple ActionPlans and choose the least costly plan.
    @Override
    public <T> ActionPlan plan(MindAgent<T> agent, HashSet<MindGoal> goals, @Nullable MindGoal recentGoal) {
        List<MindGoal> ordered = new ArrayList<>(goals.stream()
            .filter(goal -> !goal.getEffects().isEmpty() && goal.getEffects().stream().anyMatch(belief -> !belief.evaluate()))
            .toList());
        ordered.sort((goal1, goal2) -> compareGoals(goal1, goal2, recentGoal));

        try {
            for (MindGoal goal : ordered) {
                Node goalNode = new Node(null, null, goal.getEffects(), 0);
                HashSet<MindAction> availableActions = agent.getActions();
                // Add all exposed actions that are available to the Agent.
                availableActions.addAll(agent.collectAvailableActions());
                if (findPath(goalNode, availableActions)) {
                    if (goalNode.isLeafDead()) continue;

                    LinkedList<MindAction> actionStack = new LinkedList<>();
                    while (!goalNode.leaves.isEmpty()) {
                        goalNode.leaves.sort((l1, l2) -> Float.compare(l2.cost, l1.cost));
                        goalNode = goalNode.leaves.get(0);
                        actionStack.add(goalNode.action);
                    }

                    return new ActionPlan(goal, actionStack, goalNode.cost);
                }
            }
        } catch (Exception ignored) {}
        LOGGER.error("Could not find Plan for entity!");
        return null;
    }

    private boolean findPath(Node parent, HashSet<MindAction> actions) {
        SortedSet<MindAction> sorted = new TreeSet<>(BasicPlanner::compareActions);
        sorted.addAll(actions);
        for (MindAction act : sorted) {
            HashSet<MindBelief> required = parent.requiredEffects;
            required.forEach(belief -> {
                if (belief.evaluate()) required.remove(belief);
            });
            if (required.isEmpty())  {
                return true;
            }

            if (act.getEffects().stream().anyMatch(required::contains)) {
                HashSet<MindBelief> newRequired = new HashSet<>(required);
                newRequired.removeAll(act.getEffects());
                newRequired.addAll(act.getPreconditions());

                Node newNode = new Node(parent, act, newRequired, parent.cost + act.getCost());

                if (findPath(newNode, actions)) {
                    parent.leaves.add(newNode);
                    newRequired.removeAll(act.getPreconditions());
                }

                if (newRequired.isEmpty()) return true;
            }
        }
        return false;
    }

    private static int compareActions(MindAction act1, MindAction act2) {
        int originalComparison = Double.compare(act1.getCost(), act2.getCost());
        if (originalComparison != 0) return originalComparison;
        // Don't care for order between two particular objects
        return act1 == act2 || act1.equals(act2) ? 0 : -1;
    }

    private static int compareGoals(MindGoal one, MindGoal two, @Nullable MindGoal recentGoal) {
        float priority1 = one == recentGoal ? one.getPriority()-0.01F : one.getPriority();
        float priority2 = two == recentGoal ? two.getPriority()-0.01F : two.getPriority();
        return Float.compare(priority2, priority1);
    }

    public static class Node {
        @Nullable
        private final Node parent;
        @Nullable
        private final MindAction action;
        private final HashSet<MindBelief> requiredEffects;
        private final List<Node> leaves;

        private final float cost;

        public Node(@Nullable Node parent, @Nullable MindAction action, HashSet<MindBelief> required, float cost) {
            this.parent = parent;
            this.action = action;
            this.requiredEffects = required;
            this.leaves = new ArrayList<>();
            this.cost = cost;
        }

        @Nullable
        public Node getParent() {
            return this.parent;
        }

        @Nullable
        public MindAction getAction() {
            return action;
        }

        public List<Node> getLeaves() {
            return leaves;
        }

        public boolean isLeafDead() {
            return leaves.isEmpty() && action == null;
        }

        public HashSet<MindBelief> getRequiredEffects() {
            return requiredEffects;
        }

        public float getCost() {
            return cost;
        }

    }

}
