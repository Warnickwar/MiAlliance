package com.mialliance.utils;

import com.mialliance.codecs.MutableListCodec;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.List;

public class ExtraCodecs {

    public static <E extends Enum<?>> Codec<E> enumCodec(Class<E> clazz) {
        if (!clazz.isEnum()) throw new IllegalArgumentException(String.format("Class %s is not an Enum!", clazz.getName()));
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
                Number num = ops.getNumberValue(input, 3);
                if (num instanceof Integer in) {
                    E constant = clazz.getEnumConstants()[in];
                    return DataResult.success(Pair.of(constant, input));
                }
                return DataResult.error("Could not find a proper number format to deserialize to!");
            }

            @Override
            public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
                return DataResult.success(ops.createNumeric(input.ordinal()));
            }
        };
    }

    public static <V> Codec<List<V>> mutableListCodec(Codec<V> valueCodec) {
        return new MutableListCodec<>(valueCodec);
    }

}
