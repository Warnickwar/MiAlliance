package com.mialliance.mind.base.sensors;

import com.mialliance.mind.base.agents.BaseAgent;
import com.mialliance.mind.base.agents.MindOwner;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class SensorKit<O extends MindOwner> {

    private final Set<SensorProvider<O>> sensorProviders;

    public SensorKit() {
        this.sensorProviders = new LinkedHashSet<>();
    }

    public SensorKit<O> addSensor(SensorProvider<O> provider) {
        sensorProviders.add(provider);
        return this;
    }

    public void applyToAgent(BaseAgent<O> agent) {
        sensorProviders.forEach(provider -> {
            Pair<SensorKey<O, BaseSensor<O>>, SensorSupplier<O, BaseSensor<O>>> pair = provider.provide();
            agent.registerSensor(pair.getFirst(), pair.getSecond());
        });
    }

    public SensorKit<O> combine(SensorKit<O> other) {
        SensorKit<O> next = new SensorKit<>();
        this.sensorProviders.forEach(next::addSensor);
        other.sensorProviders.forEach(next::addSensor);
        return next;
    }

    public interface SensorProvider<O extends MindOwner> {
        @NotNull Pair<SensorKey<O, BaseSensor<O>>, SensorSupplier<O, BaseSensor<O>>> provide();
    }
}
