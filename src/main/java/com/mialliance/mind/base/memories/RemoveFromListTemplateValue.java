package com.mialliance.mind.base.memories;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

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

    @Override
    public void applyToMemories(MemoryManager manager) {

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public boolean compare(ImmutableMemoryManager manager) {
        List<T> val;
        boolean useIndex = valToRemove.right().isPresent();
        return (val = manager.getMemory(type)) == null || !(useIndex ? (valToRemove.right().get() > val.size() || valToRemove.right().get() < 0) : val.contains(valToRemove.left().get()));
    }

    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    @Override
    public RemoveFromListTemplateValue<T> copy() {
        boolean useIndex = valToRemove.right().isPresent();
        if (useIndex) {
            return new RemoveFromListTemplateValue<>(type, valToRemove.right().get());
        }
        return new RemoveFromListTemplateValue<>(type, valToRemove.left().get());
    }

}
