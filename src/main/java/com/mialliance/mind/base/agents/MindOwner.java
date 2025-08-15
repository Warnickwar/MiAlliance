package com.mialliance.mind.base.agents;

/**
 * A marker interface which indicates that the current object possesses a {@link BaseAgent Mind}.
 */
public interface MindOwner {
    <A extends BaseAgent<?>> A getAgent();
}
