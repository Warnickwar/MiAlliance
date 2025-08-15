package com.mialliance.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class MutableListCodec<A> implements Codec<List<A>> {

    private final Codec<A> elementCodec;

    public MutableListCodec(final Codec<A> elementCodec) {
        this.elementCodec = elementCodec;
    }
    @Override
    public <T> DataResult<T> encode(List<A> input, DynamicOps<T> ops, T prefix) {
        ListBuilder<T> builder = ops.listBuilder();

        for (final A val : input) {
            builder.add(elementCodec.encodeStart(ops, val));
        }

        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<Consumer<Consumer<T>>> temp = ops.getList(input).setLifecycle(Lifecycle.stable());
        return temp.flatMap(stream -> {
            List<A> list = new LinkedList<>();
            final Stream.Builder<T> failed = Stream.builder();

            AtomicReference<DataResult<Unit>> result = new AtomicReference<>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()));

            stream.accept(val -> {
                DataResult<Pair<A, T>> elem = elementCodec.decode(ops, val);
                elem.error().ifPresent(e -> failed.add(val));
                result.setPlain(result.get().apply2stable((unit, pair) -> {
                    list.add(pair.getFirst());
                    return unit;
                }, elem));
            });

            T errors = ops.createList(failed.build());

            final Pair<List<A>, T> pair = Pair.of(list, errors);

            return result.get().map(unit -> pair).setPartial(pair);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutableListCodec<?> that = (MutableListCodec<?>) o;
        return Objects.equals(elementCodec, that.elementCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elementCodec);
    }

    @Override
    public String toString() {
        return "MutableListCodec[" + elementCodec + ']';
    }

}
