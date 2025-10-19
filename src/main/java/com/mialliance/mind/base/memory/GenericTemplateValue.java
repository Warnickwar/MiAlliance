package com.mialliance.mind.base.memory;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
        if (isRemovable()) {
            return new GenericTemplateValue<>(type);
        } else {
            assert value.left().isPresent();
            return new GenericTemplateValue<>(type, value.left().get(), expiryTime);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SetMemory{type=").append(type);
        if (isRemovable()) {
            builder.append(",removable=true");
        } else {
            assert value.left().isPresent();
            builder.append(",value=").append(value.left().get());
        }
        return builder.append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericTemplateValue<?> that = (GenericTemplateValue<?>) o;
        return expiryTime == that.expiryTime && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value, expiryTime);
    }

}