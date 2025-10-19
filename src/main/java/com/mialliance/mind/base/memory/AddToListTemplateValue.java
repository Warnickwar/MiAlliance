package com.mialliance.mind.base.memory;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    @Override
    public String toString() {
        // Type and Value is always definitive
        StringBuilder builder = new StringBuilder("AddToList{type=").append(type).append(",value=").append(value);
        // Specific Index is uncertain, append only if not at end
        if (this.hasSpecialIndex()) builder.append(",index=").append(index);
        // Expiry Time is uncertain, append only if timed
        if (expiryTimeIfNew != Long.MAX_VALUE) builder.append(",newValueExpiryTime=").append(expiryTimeIfNew);
        return builder.append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AddToListTemplateValue<?> that = (AddToListTemplateValue<?>) o;
        return index == that.index && expiryTimeIfNew == that.expiryTimeIfNew && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, index, expiryTimeIfNew);
    }

}