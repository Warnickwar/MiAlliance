package com.mialliance.mind.base.agents;

public interface MindOwner {
    <A extends BaseAgent<?>> A getAgent();
}
