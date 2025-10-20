package com.mialliance.mind.base.action;

import com.mialliance.mind.base.agent.MindAgent;

import java.util.Set;

public interface IActionExposer {

    /**
     * <p>
     *     Return a set of available actions for the given Agent.
     *     This can be used to filter for certain Agents, EntityTypes, and other such
     *     conditionals.
     * </p>
     * <p>
     *     It is highly advised to use Builders to create new actions per valid
     *     Agent when submitting Actions, such as to have everything necessary to be exposed
     *     to the Actions properly.
     * </p>
     * @param agent The Agent requesting any exposed action.
     * @return A set of actions available for the Agent to plan with at the given moment.
     * @see MindAgent#collectAvailableActions()
     */
    Set<MindAction> exposeActions(MindAgent<?> agent);
}
