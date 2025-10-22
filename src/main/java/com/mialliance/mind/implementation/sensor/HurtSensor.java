package com.mialliance.mind.implementation.sensor;

import com.mialliance.mind.base.MindSensor;

public class HurtSensor extends MindSensor {

    /**
     * @param tickTimer How many ticks should pass before {@link MindSensor#onTick()} is run once.
     */
    protected HurtSensor(int tickTimer) {
        super(tickTimer);
    }

    @Override
    protected void onTick() {

    }

}
