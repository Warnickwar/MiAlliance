package com.mialliance.mind.base.sensors;

import com.mialliance.mind.base.agents.MindOwner;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSensor<O extends MindOwner> {

    @NotNull
    private final O owner;
    private int cooldown;

    protected BaseSensor(@NotNull O owner) {
        this.owner = owner;
        this.cooldown = 0;
    }

    @NotNull
    public final O getOwner() {
        return owner;
    }

    public boolean shouldTick() {
        return true;
    }

    public int cooldownToTick() {
        return 0;
    }

    public final void tick() {
        if (this.cooldown-- <= 0) {
            this.cooldown = cooldownToTick();
            this.onTick();
        }
    }

    public abstract void onTick();

    protected abstract void register();

    protected abstract void unregister();
}
