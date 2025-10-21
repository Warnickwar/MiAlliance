package com.mialliance.mind.base.sensor;

/**
 * <p>
 *     An object which executes and evaluates world conditions. This is to be used in {@link com.mialliance.mind.base.agent.MindAgent Agents}
 *     to allow them to collect information about the world, such as where blocks are and where entities are.
 * </p>
 */
public abstract class MindSensor {

    private final int tickTimer;
    private int ticksPassed;

    /**
     * @param tickTimer How many ticks should pass before {@link MindSensor#onTick()} is run once.
     */
    protected MindSensor(int tickTimer) {
        this.tickTimer = tickTimer;
        this.ticksPassed = this.tickTimer;
    }

    public final void tick() {
        if (ticksPassed++ >= tickTimer) {
            this.onTick();
            this.ticksPassed = 0;
        }
    }

    /**
     * What should happen when the Sensor executes.
     */
    protected abstract void onTick();

}
