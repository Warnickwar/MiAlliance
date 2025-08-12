package com.mialliance.mind.memories;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MemoryManager extends ImmutableMemoryManager {

    private MemoryManager(MemoryManager manager) {
        super(manager);
    }

    private MemoryManager(ImmutableMemoryManager manager) {
        super(manager);
    }

    public void tick() {
        this.memories.values().forEach(mem -> {
            if (mem.isExpirable()) mem.tick();
        });
    }

    public void acceptAll(ImmutableMemoryManager manager) {
        manager.getKeys().forEach(type -> {
            MemoryValue<?> val = manager.getMemoryValue(type);
            assert val != null;

            this.setMemoryInternal(type, val.copy());
        });
    }

    public <T> void removeMemory(MemoryModuleType<T> type) {
        this.memories.remove(type);
    }

    public <T> void setMemory(MemoryModuleType<T> type, T value) {
        this.setMemoryInternal(type, new MemoryValue<>(type, value));
    }

    public <T> void setMemory(MemoryModuleType<T> type, T value, long expiryTime) {
        this.setMemoryInternal(type, new MemoryValue<>(type, value, expiryTime));
    }

    private <T> void setMemoryInternal(MemoryModuleType<?> type, MemoryValue<?> value) {
        this.memories.put(type, value);
    }

    /**
     * A function provided which can expose any Memory Manager with a soft copy of its values.
     * @param immutable The manager to expose
     * @return A new, exposed Memory Manager which is mutable and has copies of the values.
     */
    public static MemoryManager of(ImmutableMemoryManager immutable) {
        return new MemoryManager(immutable);
    }
}
