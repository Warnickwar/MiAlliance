package com.mialliance.registers;

import com.mialliance.MiAlliance;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModMemoryModules {

    public static final MemoryModuleType<Unit> IDLE_HAS_MOVED = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "idle_has_moved"));

    @SuppressWarnings("deprecation")
    private static <T> MemoryModuleType<T> register(ResourceLocation id, @Nullable Codec<T> codec) {
        return Registry.register(Registry.MEMORY_MODULE_TYPE, id, new MemoryModuleType<>(Optional.ofNullable(codec)));
    }

    private static <T> MemoryModuleType<T> register(ResourceLocation id) {
        return register(id, null);
    }

}
