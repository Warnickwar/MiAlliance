package com.mialliance.mind.base.strategy;

import net.minecraft.util.Mth;

public interface IStrategy {

    void start();

    void tick();

    void stop(boolean successful);

    boolean canPerform();

    boolean isComplete();

    default int adjustedTickDelay(int currentTickDelay) {
        return Mth.positiveCeilDiv(currentTickDelay, 2);
    }
}
