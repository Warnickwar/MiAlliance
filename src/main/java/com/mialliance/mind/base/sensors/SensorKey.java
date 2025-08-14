package com.mialliance.mind.base.sensors;

import com.mialliance.mind.base.agents.MindOwner;
import net.minecraft.resources.ResourceLocation;

public final class SensorKey<O extends MindOwner, S extends BaseSensor<O>> {

    private final String identifier;

    public SensorKey(String identifier) {
        this.identifier = identifier;
    }

    public SensorKey(ResourceLocation identifier) {
        this(identifier.toString());
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
