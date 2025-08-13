package com.mialliance.mind.memories;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TemplateValue<T> {

    private final MemoryModuleType<T> type;
    private final T value;

    public TemplateValue(MemoryModuleType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public void applyToMemories(MemoryManager manager) {
        manager.setMemory(type, value);
    }

}
