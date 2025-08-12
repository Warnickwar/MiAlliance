package com.mialliance.mind.agents;

public interface MindOwner {
    <A extends BaseAgent<?>> A getAgent();
}
