package com.mialliance.mind.sensors;

import com.mialliance.mind.agents.MindOwner;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSensor<O extends MindOwner> {

    @NotNull
    private final O owner;

    protected BaseSensor(@NotNull O owner) {
        this.owner = owner;
    }

    @NotNull
    public final O getOwner() {
        return owner;
    }

    public boolean shouldTick() {
        return true;
    }

    public abstract void tick();

    protected abstract void register();

    protected abstract void unregister();
}
