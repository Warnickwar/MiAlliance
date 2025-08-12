package com.mialliance.mind.memories;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import javax.annotation.Nullable;
import java.util.Optional;

// Custom implementation for a Component-based Memory Manager
@SuppressWarnings("deprecation")
public class MemoryValue<T> {

    final MemoryModuleType<T> type;
    // I literally could not care, this is intentional
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    final Optional<? extends ExpirableValue<T>> value;

    public MemoryValue(MemoryModuleType<T> type, T value) {
        this.type = type;
        this.value = Optional.of(ExpirableValue.of(value));
    }

    public MemoryValue(MemoryModuleType<T> type, T value, long expiryTime) {
        this.type = type;
        this.value = Optional.of(ExpirableValue.of(value, expiryTime));
    }

    public MemoryValue(MemoryModuleType<T> type, ExpirableValue<T> val) {
        this.type = type;
        this.value = Optional.of(val);
    }

    @Nullable
    public T getValue() {
        return value.map(ExpirableValue::getValue).orElse(null);
    }

    public long remainingTimeToExpire() {
        return value.map(ExpirableValue::getTimeToLive).orElse(0L);
    }

    public boolean isNull() {
        return this.value.isEmpty();
    }

    public boolean hasExpired() {
        return this.value.map(ExpirableValue::hasExpired).orElse(true);
    }

    public boolean isExpirable() {
        return this.value.map(ExpirableValue::canExpire).orElse(true);
    }

    public void tick() {
        this.value.ifPresent(ExpirableValue::tick);
    }

    public <T> void serialize(DynamicOps<T> ops, RecordBuilder<T> builder) {
        this.type.getCodec().ifPresent(codec -> {
            this.value.ifPresent(val -> {
                builder.add(Registry.MEMORY_MODULE_TYPE.byNameCodec().encodeStart(ops, type), codec.encodeStart(ops, val));
            });
        });
    }

    public MemoryValue<T> copy() {
        return new MemoryValue<T>(type, value.map(ExpirableValue::getValue).orElse(null));
    }
}