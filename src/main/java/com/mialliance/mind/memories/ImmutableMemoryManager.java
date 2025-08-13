package com.mialliance.mind.memories;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
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
        values.forEach(val -> memories.put(val.type, val));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <V> V getMemory(MemoryModuleType<V> type) {
        MemoryValue<V> value = (MemoryValue<V>) memories.get(type);
        if (value == null) return null;
        return value.value.map(ExpirableValue::getValue).orElse(null);
    }

    public boolean hasMemory(MemoryModuleType<?> type) {
        return memories.containsKey(type);
    }

    public boolean hasMemoryValue(MemoryModuleType<?> type) {
        MemoryValue<?> value = memories.get(type);
        if (value == null) return false;
        return value.value.map(val -> !val.hasExpired()).orElse(false);
    }

    public boolean compareMemory(MemoryModuleType<?> type, Predicate<MemoryValue<?>> expectedFilter) {
        return expectedFilter.test(memories.get(type));
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
        MemoryValue<T> val = (MemoryValue<T>) memories.get(type);
        if (val == null && expectedValue == null) return true;
        assert val != null;
        T value = val.getValue();
        if (value == null && expectedValue == null) return true;
        assert value != null;
        return value.equals(expectedValue);
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
