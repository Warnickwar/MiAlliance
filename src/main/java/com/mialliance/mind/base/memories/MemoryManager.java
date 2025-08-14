package com.mialliance.mind.base.memories;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class MemoryManager extends ImmutableMemoryManager {

    public MemoryManager() {
        super();
    }

    private MemoryManager(ImmutableMemoryManager manager) {
        super(manager);
    }

    public void tick() {
        this.memories.values().forEach(mem -> {
            if (mem.isExpirable()) {
                if (mem.hasExpired()) {
                    removeMemory(mem.getType());
                } else {
                    mem.tick();
                }
            }
        });
    }

    public void acceptAll(ImmutableMemoryManager manager) {
        manager.getKeys().forEach(type -> {
            MemoryValue<?> val = manager.getMemoryValue(type);
            assert val != null;

            this.setMemoryInternal(type, val.copy());
        });
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T removeMemory(@NotNull MemoryModuleType<T> type) {
        MemoryValue<T> val = (MemoryValue<T>) this.memories.remove(type);
        if (val == null) return null;
        return val.getValue();
    }

    public <T> void setMemory(@NotNull MemoryModuleType<T> type, @NotNull T value) {
        this.setMemoryInternal(type, new MemoryValue<>(type, value));
    }

    public <T> void setMemory(@NotNull MemoryModuleType<T> type, @NotNull T value, long expiryTime) {
        this.setMemoryInternal(type, new MemoryValue<>(type, value, expiryTime));
    }

    private <T> void setMemoryInternal(@NotNull MemoryModuleType<?> type, @NotNull MemoryValue<?> value) {
        this.memories.put(type, value);
    }

    /**
     * A function provided which can expose any Memory Manager with a soft copy of its values.
     * @param immutable The manager to expose
     * @return A new, exposed Memory Manager which is mutable and has copies of the values.
     */
    public static MemoryManager of(@NotNull ImmutableMemoryManager immutable) {
        return new MemoryManager(immutable);
    }
}
