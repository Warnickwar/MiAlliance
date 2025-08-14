package com.mialliance.mind.base.sensors;

import com.mialliance.mind.base.agents.MindOwner;

@FunctionalInterface
public interface SensorSupplier<O extends MindOwner, T extends BaseSensor<O>> {
    T create(O owner);
}
