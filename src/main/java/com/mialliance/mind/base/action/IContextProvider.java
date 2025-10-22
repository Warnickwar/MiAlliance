package com.mialliance.mind.base.action;

import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.kits.PlanContext;

public interface IContextProvider {

    /**
     * <p>
     *     This method is called by the Agent's Planner and is used to collect all information on
     *     what an Agent can do. Using conditionals, developers can implement this function
     *     to only allow certain agents to use a goal or action.
     * </p>
     * <p>
     *     {@link com.mialliance.mind.base.belief.MindBelief Beliefs} for both injected Goals and Actions
     *     do not have to be on the agent's perspective. Rather, it can be from the implementor's perspective-
     *     missing items or low statistics for instance.
     * </p>
     * <p>
     *     In the case of beliefs desiring information from the Agent, the agent is provided within the
     *     context given.
     * </p>
     * @param context The information used to consider what to add to the context while
     *                an Agent is planning.
     * @see MindAgent#collectContext()
     * @see PlanContext
     */
    <T> void injectContext(PlanContext<MindAgent<T>> context);
}
