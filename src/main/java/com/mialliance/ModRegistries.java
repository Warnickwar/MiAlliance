package com.mialliance;

import com.mialliance.buildings.BuildingType;
import com.mialliance.components.ComponentType;
import com.mialliance.mind.base.kits.Behavior;
import com.mialliance.mixins.RegistryAccessor;
import com.mialliance.registers.ModBehaviors;
import com.mialliance.registers.ModComponents;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ModRegistries {

    public static class KEYS {

        public static final ResourceKey<Registry<BuildingType<?>>> BUILDINGS = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "buildings"));
        public static final ResourceKey<Registry<ComponentType<?, ?>>> COMPONENTS = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "components"));
        public static final ResourceKey<Registry<Behavior>> MIND_BEHAVIORS = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "behaviors"));

    }

    public static class REGISTRIES {

        @SuppressWarnings("unchecked")
        private static final WritableRegistry<WritableRegistry<?>> ROOT = (WritableRegistry<WritableRegistry<?>>) Registry.REGISTRY;

//        public static final Registry<BuildingType<?>> BUILDINGS;
        public static final Registry<ComponentType<?, ?>> COMPONENTS;
        public static final Registry<Behavior> MIND_BEHAVIORS;

        static {
            COMPONENTS = create(KEYS.COMPONENTS, (reg) -> ModComponents.GENERIC.DUMMY);
            MIND_BEHAVIORS = create(KEYS.MIND_BEHAVIORS, (reg) -> ModBehaviors.DUMMY);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static <T> Registry<T> create(ResourceKey<Registry<T>> key, RegistryBootstrap<T> bootstrap) {
            MappedRegistry<T> reg = new MappedRegistry<>(key, Lifecycle.experimental(), null);
            assert RegistryAccessor.mialliance$getLoaders() != null;
            RegistryAccessor.mialliance$getLoaders().put(key.location(), () -> {
                return bootstrap.run(reg);
            });
            ROOT.register((ResourceKey) key, reg, Lifecycle.experimental());
            return reg;
        }

        // Load class so that the engine is aware of our registries
        public static void load() {}

        @FunctionalInterface
        public interface RegistryBootstrap<T> {
            T run(Registry<T> registry);
        }
    }
}
