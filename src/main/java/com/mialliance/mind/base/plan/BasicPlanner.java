package com.mialliance.mind.base.plan;

import com.mialliance.mind.base.action.MindAction;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.belief.MindBelief;
import com.mialliance.mind.base.MindGoal;
import com.mialliance.mind.base.kits.PlanContext;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class BasicPlanner implements IPlanner {

    private static final Logger LOGGER = LogUtils.getLogger();

    // TODO: Make Planner evaluate multiple ActionPlans and choose the least costly plan.
    @Override
    public <T> ActionPlan plan(MindAgent<T> agent, HashSet<MindGoal> goals, @Nullable MindGoal recentGoal) {
        List<MindGoal> ordered = new ArrayList<>();
        PlanContext<MindAgent<T>> context = agent.collectContext();
        // Apply all contextual goals that are available to the Agent.
        // Filter goals, just in case, so we can avoid completing goals that are
        //  Contextual that are already complete.
        ordered.addAll(context.getGoals().stream()
            .filter(BasicPlanner::filterGoal)
            .toList());
        ordered.addAll(goals.stream()
            .filter(BasicPlanner::filterGoal)
            .toList());
        // Order all goals, contextual and otherwise, by priority
        ordered.sort((goal1, goal2) -> compareGoals(goal1, goal2, recentGoal));

        try {
            for (MindGoal goal : ordered) {
                Node goalNode = new Node(null, null, goal.getEffects(), 0);
                HashSet<MindAction> availableActions = agent.getActions();
                // Add all contextual actions that are available to the Agent.
                availableActions.addAll(context.getActions());
                if (findPath(goalNode, availableActions)) {
                    if (goalNode.isLeafDead()) continue;

                    LinkedList<MindAction> actionStack = new LinkedList<>();
                    Node currentNode = goalNode;
                    while (!currentNode.leaves.isEmpty()) {
                        currentNode.leaves.sort(BasicPlanner::compareCost);
                        currentNode = currentNode.leaves.get(0);
                        actionStack.add(0, currentNode.action);
                    }

                    return new ActionPlan(goal, actionStack, goalNode.cost);
                }
            }
        } catch (Exception ignored) {

        }
        LOGGER.error("Could not find Plan for Agent {}!", agent.getOwner().toString());
        return null;
    }

    private static boolean findPath(Node parent, HashSet<MindAction> actions) {
        SortedSet<MindAction> sorted = new TreeSet<>(BasicPlanner::compareCost);
        sorted.addAll(actions);
        for (MindAction act : sorted) {
            HashSet<MindBelief> required = new HashSet<>(parent.requiredEffects);
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
                    parent.leaves.add(0, newNode);
                    newRequired.removeAll(act.getPreconditions());
                }

                if (newRequired.isEmpty()) return true;
            }
        }
        return false;
    }

    private static boolean filterGoal(@NotNull MindGoal goal) {
        return !goal.getEffects().isEmpty() && goal.getEffects().stream().anyMatch(belief -> !belief.evaluate());
    }

    private static int compareGoals(MindGoal one, MindGoal two, @Nullable MindGoal recentGoal) {
        float priority1 = one == recentGoal ? one.getPriority()-0.01F : one.getPriority();
        float priority2 = two == recentGoal ? two.getPriority()-0.01F : two.getPriority();
        // Move the goal down the line instead of removing at redundant points
        return priority1 < priority2 ? 1 : -1;
    }

    private static int compareCost(MindAction act1, MindAction act2) {
        float costOne = act1.getCost();
        float costTwo = act2.getCost();
        return costOne < costTwo ? -1 : 1;
    }

    private static int compareCost(Node one, Node two) {
        float costOne = one.cost;
        float costTwo = two.cost;
        return costOne < costTwo ? -1 : 1;
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
