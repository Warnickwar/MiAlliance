package com.mialliance.mind.base.memories;

import com.google.common.collect.ImmutableSet;
import com.mialliance.mind.base.NullablePredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class ImmutableMemoryManager {

    public static final Codec<ImmutableMemoryManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        MemoryValue.CODEC.listOf().fieldOf("memories").forGetter(ImmutableMemoryManager::getValues)
    ).apply(inst, ImmutableMemoryManager::new));

    protected final LinkedHashMap<MemoryModuleType<?>, MemoryValue<?>> memories;

    public ImmutableMemoryManager() {
        this.memories = new LinkedHashMap<>();
    }

    protected ImmutableMemoryManager(ImmutableMemoryManager original) {
        this.memories = new LinkedHashMap<>();
        original.memories.forEach((type, val) -> {
            this.memories.put(type, val.copy());
        });
    }

    public ImmutableMemoryManager(LinkedHashMap<MemoryModuleType<?>, MemoryValue<?>> values, Supplier<Codec<ImmutableMemoryManager>> codecSupp) {
        this.memories = values;
    }

    private ImmutableMemoryManager(List<MemoryValue<?>> values) {
        this.memories = new LinkedHashMap<>();
        values.forEach(val -> memories.put(val.getType(), val));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <V> V getMemory(MemoryModuleType<V> type) {
        return (V) memories.get(type);
    }

    public boolean hasMemory(MemoryModuleType<?> type) {
        MemoryValue<?> val;
        // Mapped in Manager and has not expired
        return (val = memories.get(type)) != null && !val.hasExpired();
    }

    public <T> boolean compareMemory(MemoryModuleType<T> type, NullablePredicate<T> expectedFilter) {
        return expectedFilter.test(this.getMemory(type));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected <T> MemoryValue<T> getMemoryValue(MemoryModuleType<T> type) {
        return (MemoryValue<T>) memories.get(type);
    }

    // This is stupid-Optimize later.
    // - Warnickwar
    @SuppressWarnings("unchecked")
    public <T> boolean compareMemory(MemoryModuleType<T> type, @Nullable T expectedValue) {
        MemoryValue<T> memoryVal = (MemoryValue<T>) memories.get(type);
        if (memoryVal == null && expectedValue == null) return true;
        assert memoryVal != null;
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
