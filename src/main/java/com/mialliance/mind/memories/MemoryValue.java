package com.mialliance.mind.memories;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

// Custom implementation of a Memory Value for a Component-based Memory Manager
@SuppressWarnings("deprecation")
public class MemoryValue<T> {

    // FINA-FUCKING-LY this is adequate enough.
    // - Warnickwar

    public static final Codec<MemoryValue<?>> CODEC = new Codec<>() {

        @Override
        public <K> DataResult<K> encode(MemoryValue<?> input, DynamicOps<K> ops, K prefix) {
            if (input.value.isEmpty()) return DataResult.error("Could not find a value to encode!");
            Codec<MemoryModuleType<?>> typeCodec = Registry.MEMORY_MODULE_TYPE.byNameCodec();
            Optional<Codec<ExpirableValue<?>>> valueOptional = input.getGenericCodec();
            if (valueOptional.isEmpty()) return DataResult.error("Could not a Codec to encode with!");
            Codec<ExpirableValue<?>> valueCodec = valueOptional.get();
            RecordBuilder<K> map = ops.mapBuilder().add("type", typeCodec.encodeStart(ops, input.type)).add("value", valueCodec.encodeStart(ops, input.value.get()));
            map.build(prefix);
            return DataResult.success(prefix);
        }

        @Override
        public <K> DataResult<Pair<MemoryValue<?>, K>> decode(DynamicOps<K> ops, K input) {
            DataResult<MapLike<K>> mapRes = ops.getMap(input);
            if (mapRes.result().isEmpty()) return DataResult.error("Could not parse the memory map!");
            MapLike<K> map = mapRes.result().get();
            K typeName = map.get("type");
            DataResult<MemoryModuleType<?>> typeRes = Registry.MEMORY_MODULE_TYPE.byNameCodec().parse(ops, typeName);
            if (typeRes.result().isEmpty()) return DataResult.error("Could not find a Memory type to parse from!");
            MemoryModuleType<?> type = typeRes.result().get();
            AtomicReference<DataResult<ExpirableValue<?>>> finalRes = new AtomicReference<>(DataResult.error(""));
            type.getCodec().map(codec -> codec.decode(ops, map.get("value"))).flatMap(DataResult::result).ifPresent(pair -> {
                finalRes.set(DataResult.success(pair.getFirst()));
            });

            if (finalRes.get().result().isEmpty()) return DataResult.error("Could not parse a value for memory type " + Registry.MEMORY_MODULE_TYPE.getKey(type) + "!");

            // This is fine because we don't care the actual underlying type, we just care to decode it.
            //noinspection rawtypes,unchecked
            MemoryValue<?> finalValue = new MemoryValue<>(type, finalRes.get().result().get());

            Pair<MemoryValue<?>, K> finalPair = Pair.of(finalValue, input);
            return DataResult.success(finalPair);
        }
    };

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

    // This is such a disgusting hack, but fuck it, I'm lazy.
    @SuppressWarnings("unchecked")
    private Optional<Codec<ExpirableValue<?>>> getGenericCodec() {
        Optional<Codec<ExpirableValue<T>>> codec = this.type.getCodec();
        if (codec.isEmpty()) return Optional.empty();
        Codec<?> castCodec = codec.get();
        return Optional.of((Codec<ExpirableValue<?>>) castCodec);
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