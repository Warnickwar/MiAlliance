package com.mialliance.mind.base.sensor;

public abstract class MindSensor {

    protected final int tickTimer;
    private int ticksPassed;

    public MindSensor(int tickTimer) {
        this.tickTimer = tickTimer;
        this.ticksPassed = 0;
    }

    public final void tick() {
        if (ticksPassed++ >= tickTimer) {
            this.onTick();
            this.ticksPassed = 0;
        }
    }

    protected abstract void onTick();

}
