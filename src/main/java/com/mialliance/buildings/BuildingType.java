package com.mialliance.buildings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

// TODO: Make actually functional.
public class BuildingType<T extends Building> {

    protected final BuildingFactory<T> buildSupplier;

    protected BuildingType(BuildingFactory<T> buildSupplier) {
        this.buildSupplier = buildSupplier;
    }

    public T create(ServerLevel level, BlockPos position) {
        return this.create(level, position, true);
    }

    public T create(ServerLevel level, BlockPos position, boolean shouldTick) {
        T build = this.buildSupplier.create(this, level, position);
        return build;
    }

    // TODO: Allow definition of rotation by Direction, assume North for default rotation
    public T create(ServerLevel level, BlockPos position, Direction dir) {
        return this.create(level, position, dir, true);
    }

    public T create(ServerLevel level, BlockPos position, Direction dir, boolean shouldTick) {
        return this.buildSupplier.create(this, level, position);
    }

    public static <T extends Building> Builder<T> of(BuildingFactory<T> factory) {
        return new BuildingType.Builder<T>(factory);
    }

    public static <T extends Building> Optional<ResourceLocation> by(BuildingType<T> type) {
//        return Optional.ofNullable(ModRegistries.REGISTRIES.BUILDINGS.getKey(type));
        return Optional.empty();
    }

    public static Optional<ResourceLocation> by(CompoundTag tag) {
//        BuildingType<?> type = ModRegistries.REGISTRIES.BUILDINGS.get(ResourceLocation.parse(tag.getString("Type")));
//        return Optional.ofNullable(type != null ? ModRegistries.REGISTRIES.BUILDINGS.getKey(type) : null);
        return Optional.empty();
    }

    @FunctionalInterface
    public interface BuildingFactory<T extends Building> {
        T create(BuildingType<T> type, ServerLevel level, BlockPos position);
    }

    public static class Builder<B extends Building> {

        private final BuildingType<B> type;

        private Builder(BuildingFactory<B> fact) {
            this.type = new BuildingType<>(fact);
        }

        public BuildingType<B> construct() {
            return type;
        }
    }
}
