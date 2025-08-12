package com.mialliance.mind.memories;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TemplateValue<T> {

    private final MemoryModuleType<T> type;
    private final T value;
    private final long expiryTime;

    public TemplateValue(MemoryModuleType<T> type, T value) {
        this(type, value, Long.MAX_VALUE);
    }

    public TemplateValue(MemoryModuleType<T> type, T value, long expiry) {
        this.type = type;
        this.value = value;
        this.expiryTime = expiry;
    }

    public void applyToMemories(MemoryManager manager) {
        if (expiryTime == Long.MAX_VALUE) {
            manager.setMemory(type, value);
        } else {
            manager.setMemory(type, value, expiryTime);
        }
    }

}
