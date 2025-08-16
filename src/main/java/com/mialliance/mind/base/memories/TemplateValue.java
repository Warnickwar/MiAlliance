package com.mialliance.mind.base.memories;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;
import java.util.Objects;

public abstract class TemplateValue<T> {

    private static final long notExpirable = Long.MAX_VALUE;

    protected final MemoryModuleType<T> type;

    protected TemplateValue(MemoryModuleType<T> type) {
        this.type = type;
    }

    public abstract boolean isRemovable();

    public abstract void applyToMemories(MemoryManager manager);

    public abstract boolean compare(ImmutableMemoryManager manager);

    public abstract <V extends TemplateValue<T>> V copy();

    public static <T> TemplateValue<T> addExpirableMemory(MemoryModuleType<T> type, T value, long expiryTime) {
        return new GenericTemplateValue<>(type, value, expiryTime);
    }

    public static <T> TemplateValue<T> addMemory(MemoryModuleType<T> type, T value) {
        return addExpirableMemory(type, value, notExpirable);
    }

    public static <T> TemplateValue<T> removeMemory(MemoryModuleType<T> type) {
        return new GenericTemplateValue<>(type);
    }

    public static <T> TemplateValue<List<T>> addToListMemory(MemoryModuleType<List<T>> type, T value, int index, long expiryTimeIfNew) {
        return new AddToListTemplateValue<>(type, value, index, expiryTimeIfNew);
    }

    public static <T> TemplateValue<List<T>> addToListMemory(MemoryModuleType<List<T>> type, T value, int index) {
        return addToListMemory(type, value, index, Long.MAX_VALUE);
    }

    public static <T> TemplateValue<List<T>> addToListMemory(MemoryModuleType<List<T>> type, T value) {
        return addToListMemory(type, value, -1);
    }

    public static <T> TemplateValue<List<T>> removeFromListMemory(MemoryModuleType<List<T>> type, T value) {
        return new RemoveFromListTemplateValue<>(type, value);
    }

    public static <T> TemplateValue<List<T>> removeFromListMemory(MemoryModuleType<List<T>> type, int index) {
        return new RemoveFromListTemplateValue<>(type, index);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TemplateValue<?> that = (TemplateValue<?>) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    protected enum RemoveMemory {
        INSTANCE;
    }
}
