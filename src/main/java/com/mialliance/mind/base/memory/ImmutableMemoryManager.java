package com.mialliance.mind.base.memory;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ImmutableMemoryManager {

    public static final Codec<ImmutableMemoryManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        MemoryValue.CODEC.listOf().fieldOf("memories").forGetter(ImmutableMemoryManager::getValues)
    ).apply(inst, ImmutableMemoryManager::new));

    protected final ConcurrentHashMap<MemoryModuleType<?>, MemoryValue<?>> memories;

    public ImmutableMemoryManager() {
        this.memories = new ConcurrentHashMap<>();
    }

    protected ImmutableMemoryManager(ImmutableMemoryManager original) {
        this.memories = new ConcurrentHashMap<>();
        original.memories.forEach((type, val) -> {
            this.memories.put(type, val.copy());
        });
    }

    protected ImmutableMemoryManager(List<MemoryValue<?>> values) {
        this.memories = new ConcurrentHashMap<>();
        values.forEach(val -> memories.put(val.getType(), val));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <V> V getMemory(MemoryModuleType<V> type) {
        try {
            return (V) memories.get(type);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasMemory(@NotNull MemoryModuleType<?> type) {
        MemoryValue<?> val;
        // Mapped in Manager and has not expired
        return (val = memories.get(type)) != null && !val.hasExpired();
    }

    public <T> boolean compareMemory(@NotNull MemoryModuleType<T> type, @NotNull NullablePredicate<T> expectedFilter) {
        return expectedFilter.test(this.getMemory(type));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected <T> MemoryValue<T> getMemoryValue(@NotNull MemoryModuleType<T> type) {
        return (MemoryValue<T>) memories.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T> boolean compareMemory(@NotNull MemoryModuleType<T> type, @Nullable T expectedValue) {
        MemoryValue<T> memoryVal = (MemoryValue<T>) memories.get(type);
        if (memoryVal == expectedValue) return true;
        if (memoryVal == null) return false;
        return memoryVal.getValue().equals(expectedValue);
    }

    private List<MemoryValue<?>> getValues() {
        return memories.values().stream().toList();
    }

    public Collection<MemoryModuleType<?>> getKeys() {
        return ImmutableSet.copyOf(memories.keySet());
    }

    // A simple function to create an immutable view
    public static ImmutableMemoryManager of(@NotNull ImmutableMemoryManager manager) {
        return new ImmutableMemoryManager(manager);
    }

}