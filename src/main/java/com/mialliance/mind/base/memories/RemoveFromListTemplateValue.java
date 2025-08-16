package com.mialliance.mind.base.memories;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;
import java.util.Objects;

public class RemoveFromListTemplateValue<T> extends TemplateValue <List<T>>  {

    private final Either<T, Integer> valToRemove;

    protected RemoveFromListTemplateValue(MemoryModuleType<List<T>> type, T valueToRemove) {
        super(type);
        this.valToRemove = Either.left(valueToRemove);
    }

    protected RemoveFromListTemplateValue(MemoryModuleType<List<T>> type, int indexToRemove) {
        super(type);
        this.valToRemove = Either.right(indexToRemove);
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void applyToMemories(MemoryManager manager) {
        List<T> value = manager.getMemory(type);
        if (value == null) return;
        if (isByIndex()) {
            int index = valToRemove.right().get();
            value.remove(index);
        } else {
            T obj = valToRemove.left().get();
            value.remove(obj);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean compare(ImmutableMemoryManager manager) {
        List<T> val;
        return (val = manager.getMemory(type)) == null || !(isByIndex() ? (valToRemove.right().get() > val.size() || valToRemove.right().get() < 0) : val.contains(valToRemove.left().get()));
    }

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Override
    public RemoveFromListTemplateValue<T> copy() {
        if (isByIndex()) {
            return new RemoveFromListTemplateValue<>(type, valToRemove.right().get());
        }
        return new RemoveFromListTemplateValue<>(type, valToRemove.left().get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("RemoveFromList{type=").append(type);
        // Prevent needless reevaluation, even if small
        boolean index = isByIndex();
        String val = index ? valToRemove.right().get().toString() : valToRemove.left().get().toString();
        builder.append(',').append(index ? "index=" : "value=").append(val);
        return builder.append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RemoveFromListTemplateValue<?> that = (RemoveFromListTemplateValue<?>) o;
        return Objects.equals(valToRemove, that.valToRemove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), valToRemove);
    }

    private boolean isByIndex() {
        return valToRemove.right().isPresent();
    }
}
