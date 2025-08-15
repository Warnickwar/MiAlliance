package com.mialliance.mind.base.memories;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class AddToListTemplateValue<T> extends TemplateValue<List<T>> {

    private final T value;
    private final int index;
    private final long expiryTimeIfNew;

    protected AddToListTemplateValue(@NotNull MemoryModuleType<List<T>> type, @NotNull T value) {
        this(type, value, -1);
    }

    protected AddToListTemplateValue(@NotNull MemoryModuleType<List<T>> type, @NotNull T value, int index) {
        this(type, value, index, Long.MAX_VALUE);
    }

    protected AddToListTemplateValue(@NotNull MemoryModuleType<List<T>> type, @NotNull T value, int index, long expiryTimeIfNew) {
        super(type);
        this.value = value;
        this.index = index;
        this.expiryTimeIfNew = expiryTimeIfNew;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public void applyToMemories(MemoryManager manager) {
        List<T> list = manager.getMemory(type);
        boolean shouldInjectNew = false;
        if (list == null) {
            // Make a new List
            shouldInjectNew = true;
            list = new LinkedList<>();
        }
        if (hasSpecialIndex() && isInRange(list)) {
            list.add(index, value);
        } else if (!list.contains(value)) {
            // Avoid putting a duplicate instance of the same object in the List
            list.add(value);
        }
        if (shouldInjectNew) {
            manager.setMemory(type, list, expiryTimeIfNew);
        }
    }

    @Override
    public boolean compare(ImmutableMemoryManager manager) {
        List<T> val;
        return (val = manager.getMemory(type)) != null && val.contains(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AddToListTemplateValue<T> copy() {
        return new AddToListTemplateValue<>(type, value, index, expiryTimeIfNew);
    }

    private boolean hasSpecialIndex() {
        return index != -1;
    }

    private boolean isInRange(List<T> list) {
        return list.size() > index && index < 0;
    }

}
