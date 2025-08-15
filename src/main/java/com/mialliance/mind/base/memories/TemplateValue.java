package com.mialliance.mind.base.memories;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class TemplateValue<T> {

    private static final long notExpirable = Long.MAX_VALUE;

    private final MemoryModuleType<T> type;
    private final Either<T, RemoveMemory> value;
    private final long expiryTime;

    private TemplateValue(MemoryModuleType<T> type, T value, long expiryTime) {
        this.type = type;
        this.value = Either.left(value);
        this.expiryTime = expiryTime;
    }

    private TemplateValue(MemoryModuleType<T> type, Either<T, RemoveMemory> value, long expiryTime) {
        this.type = type;
        this.value = value.left().<Either<T, RemoveMemory>>map(Either::left).orElseGet(() -> Either.right(RemoveMemory.INSTANCE));
        this.expiryTime = expiryTime;
    }

    private TemplateValue(MemoryModuleType<T> type) {
        this.type = type;
        this.value = Either.right(RemoveMemory.INSTANCE);
        this.expiryTime = notExpirable;
    }

    public boolean isRemovable() {
        return value.right().isPresent();
    }

    public void applyToMemories(MemoryManager manager) {
        value.ifLeft(val -> {
            // If not expirable, it's already set to the Max Value- aka not registered as expirable
            manager.setMemory(type, val, expiryTime);
        }).ifRight(marker -> manager.removeMemory(type));
    }

    public boolean compare(ImmutableMemoryManager manager) {
        return (value.left().isPresent() && manager.compareMemory(type, value.left().get())) && (value.right().isPresent() && !manager.hasMemory(type));
    }

    public TemplateValue<T> copy() {
        return new TemplateValue<>(type, value, expiryTime);
    }

    public static <T> TemplateValue<T> additiveExpirableMemory(MemoryModuleType<T> type, T value, long expiryTime) {
        return new TemplateValue<>(type, value, expiryTime);
    }

    public static <T> TemplateValue<T> additiveMemory(MemoryModuleType<T> type, T value) {
        return additiveExpirableMemory(type, value, notExpirable);
    }

    public static <T> TemplateValue<T> removableMemory(MemoryModuleType<T> type) {
        return new TemplateValue<>(type);
    }

    private enum RemoveMemory {
        INSTANCE
    }
}
