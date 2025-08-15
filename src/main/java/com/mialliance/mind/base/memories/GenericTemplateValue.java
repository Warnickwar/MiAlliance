package com.mialliance.mind.base.memories;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

public class GenericTemplateValue<T> extends TemplateValue<T> {

    private final Either<T, RemoveMemory> value;
    private final long expiryTime;

    protected GenericTemplateValue(@NotNull MemoryModuleType<T> type, @NotNull T value, long expiryTime) {
        super(type);
        this.value = Either.left(value);
        this.expiryTime = expiryTime;
    }

    protected GenericTemplateValue(@NotNull MemoryModuleType<T> type, @NotNull T value) {
        this(type, value, Long.MAX_VALUE);
    }

    protected GenericTemplateValue(@NotNull MemoryModuleType<T> type) {
        super(type);
        this.value = Either.right(RemoveMemory.INSTANCE);
        this.expiryTime = Long.MAX_VALUE;
    }

    @Override
    public void applyToMemories(MemoryManager manager) {
        value.ifLeft(val -> {
            // If not expirable, it's already set to the Max Value- aka not registered as expirable
            manager.setMemory(type, val, expiryTime);
        }).ifRight(marker -> manager.removeMemory(type));
    }

    @Override
    public boolean compare(ImmutableMemoryManager manager) {
        return (value.left().isPresent() && manager.compareMemory(type, value.left().get())) && (value.right().isPresent() && !manager.hasMemory(type));
    }

    @Override
    public boolean isRemovable() {
        return value.right().isPresent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public GenericTemplateValue<T> copy() {
        if (this.value.right().isPresent()) {
            return new GenericTemplateValue<>(type);
        } else {
            assert value.left().isPresent();
            return new GenericTemplateValue<>(type, value.left().get(), expiryTime);
        }
    }

}
