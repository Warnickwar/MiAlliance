package com.mialliance.mind.base.memories;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

// Custom implementation of a Memory Value for a Component-based Memory Manager
@SuppressWarnings("deprecation")
public class MemoryValue<T> {

    // FINA-FUCKING-LY this is adequate.
    // - Warnickwar

    public static final Codec<MemoryValue<?>> CODEC = new Codec<>() {

        @Override
        public <K> DataResult<K> encode(MemoryValue<?> input, DynamicOps<K> ops, K prefix) {
            Codec<MemoryModuleType<?>> typeCodec = Registry.MEMORY_MODULE_TYPE.byNameCodec();
            Optional<Codec<ExpirableValue<?>>> valueOptional = input.getGenericCodec();
            // Don't return error here as it is intentional that some Memories don't have Codecs
            if (valueOptional.isEmpty()) return DataResult.success(prefix);
            Codec<ExpirableValue<?>> valueCodec = valueOptional.get();
            RecordBuilder<K> map = ops.mapBuilder().add("type", typeCodec.encodeStart(ops, input.type)).add("value", valueCodec.encodeStart(ops, input.value));
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
            ExpirableValue<?> tempResult = finalRes.get().result().get();
            MemoryValue<?> finalValue = MemoryValue.from(type, tempResult);

            Pair<MemoryValue<?>, K> finalPair = Pair.of(finalValue, input);
            return DataResult.success(finalPair);
        }
    };

    @NotNull
    private final MemoryModuleType<T> type;
    // I literally could not care, this is intentional
    @NotNull
    private final ExpirableValue<T> value;

    public MemoryValue(@NotNull MemoryModuleType<T> type, @NotNull T value) {
        this.type = type;
        this.value = net.minecraft.world.entity.ai.memory.ExpirableValue.of(value);
    }

    public MemoryValue(@NotNull MemoryModuleType<T> type, @NotNull T value, long expiryTime) {
        this.type = type;
        this.value = ExpirableValue.of(value, expiryTime);
    }

    public MemoryValue(@NotNull MemoryModuleType<T> type, @NotNull ExpirableValue<T> val) {
        this.type = type;
        this.value = val;
    }

    public T getValue() {
        return this.value.getValue();
    }

    public @NotNull MemoryModuleType<T> getType() {
        return this.type;
    }

    public long remainingTimeToExpire() {
        return this.value.getTimeToLive();
    }

    public boolean hasExpired() {
        return this.value.hasExpired();
    }

    public boolean isExpirable() {
        return this.value.canExpire();
    }

    public void tick() {
        this.value.tick();
    }


    public MemoryValue<T> copy() {
        return new MemoryValue<>(type, value.getValue(), value.getTimeToLive());
    }

    // This is such a disgusting hack, but fuck it, I'm lazy.
    @SuppressWarnings("unchecked")
    private Optional<Codec<ExpirableValue<?>>> getGenericCodec() {
        Optional<Codec<ExpirableValue<T>>> codec = this.type.getCodec();
        if (codec.isEmpty()) return Optional.empty();
        Codec<?> castCodec = codec.get();
        return Optional.of((Codec<ExpirableValue<?>>) castCodec);
    }

    // A hack of a function needed so that the JVM Compiler doesn't fucken yell.
    //  Functionally, this makes no difference in decoding.
    @SuppressWarnings("unchecked")
    private static MemoryValue<?> from(@NotNull MemoryModuleType<?> type, ExpirableValue<?> val) {
        return new MemoryValue<>((MemoryModuleType<Object>) type, val);
    }
}