package com.mialliance.mind.base.sensors;

import com.mialliance.mind.base.agents.MindOwner;

import java.util.HashMap;
import java.util.Map;

public class SensorManager<O extends MindOwner> {

    private final Map<SensorKey<O, ?>, BaseSensor<O>> sensors;

    public SensorManager() {
        this.sensors = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseSensor<O>> void registerSensor(SensorKey<O, T> key, T sensor) {
        T previousSensor = (T) this.sensors.get(key);
        if (previousSensor != null) previousSensor.unregister();

        sensor.register();
        this.sensors.put(key, sensor);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseSensor<O>> void unregisterSensor(SensorKey<O, T> key) {
        T sensor = (T) this.sensors.remove(key);
        if (sensor != null) sensor.unregister();
    }

    public void tick() {
        sensors.values().forEach(sensor -> {
            if (sensor.shouldTick()) sensor.tick();
        });
    }

}
