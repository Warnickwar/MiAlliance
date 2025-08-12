package com.mialliance.mind.sensors;

import com.mialliance.mind.agents.MindOwner;

@FunctionalInterface
public interface SensorSupplier<O extends MindOwner, T extends BaseSensor<O>> {
    T create(O owner);
}
