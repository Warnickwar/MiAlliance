package com.mialliance.mind.memories;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.*;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ImmutableMemoryManager {

    private final Codec<ImmutableMemoryManager> CODEC;

    protected final LinkedHashMap<MemoryModuleType<?>, MemoryValue<?>> memories;

    public ImmutableMemoryManager() {
        this.memories = new LinkedHashMap<>();
        this.CODEC = codec(this);
    }

    protected ImmutableMemoryManager(ImmutableMemoryManager original) {
        this.memories = new LinkedHashMap<>();
        this.CODEC = codec(this);
        original.memories.forEach((type, val) -> {
            this.memories.put(type, val.copy());
        });
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

    public Collection<MemoryModuleType<?>> getKeys() {
        return ImmutableSet.copyOf(memories.keySet());
    }

    public <T> DataResult<T> serialize(DynamicOps<T> ops) {
        return this.CODEC.encodeStart(ops, this);
    }

    // SERIALIZATION UTILITY

    private static Codec<ImmutableMemoryManager> codec(ImmutableMemoryManager manager) {
        final MutableObject<Codec<ImmutableMemoryManager>> mutable = new MutableObject<>();
        mutable.setValue((new MapCodec<ImmutableMemoryManager>() {

            @Override
            public <T> RecordBuilder<T> encode(ImmutableMemoryManager input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                input.memories.forEach((type, val) -> {
                    val.serialize(ops, prefix);
                });
                return  prefix;
            }

            @Override
            public <T> DataResult<ImmutableMemoryManager> decode(DynamicOps<T> ops, MapLike<T> input) {
                List<MemoryValue<?>> values = new ArrayList<>();
                input.entries().forEach(pair -> {
                    // TODO: Update in 1.20.1.
                    //noinspection deprecation
                    DataResult<MemoryModuleType<?>> typeResult = Registry.MEMORY_MODULE_TYPE.byNameCodec().parse(ops, pair.getFirst());
                    DataResult<? extends MemoryValue<?>> valueResult = typeResult.flatMap(type -> {
                        return capture(type, ops, pair.getSecond());
                    });
                    // TODO: Check if this causes errors, I'm lazy rn.
                    //  - Warnickwar
                    //noinspection OptionalGetWithoutIsPresent
                    values.add(valueResult.get().left().get());
                });
                ImmutableMemoryManager mem = new ImmutableMemoryManager();
                values.forEach(val -> {
                    mem.memories.put(val.type, val);
                });
                return DataResult.success(mem);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return manager.memories.keySet().stream().flatMap(type -> {
                    return type.getCodec().map(codec -> {
                        // TODO: Fuck forge, swap to updated version in BuiltInRegistries in 1.20.1
                        return Registry.MEMORY_MODULE_TYPE.getKey(type);
                    }).stream();
                }).map(loc -> ops.createString(loc.toString()));
            }

            private <T, U> DataResult<MemoryValue<U>> capture(MemoryModuleType<U> type, DynamicOps<T> ops, T val) {
                return type.getCodec().map(DataResult::success).orElseGet(() -> {
                    return DataResult.error("No codec for Memory type: " + type);
                }).flatMap(codec -> {
                    return codec.parse(ops, val);
                }).map(expirVal -> {
                    return new MemoryValue<U>(type, expirVal);
                });
            }
        }).fieldOf("memories").codec());
        return mutable.getValue();
    }

    // A simple function to create an immutable view
    public static ImmutableMemoryManager of(ImmutableMemoryManager manager) {
        return new ImmutableMemoryManager(manager);
    }


}
