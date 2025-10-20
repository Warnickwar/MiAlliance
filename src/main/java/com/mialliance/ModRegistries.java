package com.mialliance;

import com.mialliance.buildings.BuildingType;
import com.mialliance.components.ComponentType;
import com.mialliance.mind.base.kits.Behavior;
import com.mialliance.registers.ModBehaviors;
import com.mialliance.registers.ModComponents;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ModRegistries {

    public static class KEYS {

        public static final ResourceKey<Registry<BuildingType<?>>> BUILDINGS = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "buildings"));
        public static final ResourceKey<Registry<ComponentType<?, ?>>> COMPONENTS = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "components"));
        public static final ResourceKey<Registry<Behavior<?>>> MIND_BEHAVIORS = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "behaviors"));

    }

    public static class REGISTRIES {

        public static final Registry<BuildingType<?>> BUILDINGS;
        public static final Registry<ComponentType<?, ?>> COMPONENTS;
        public static final Registry<Behavior<?>> MIND_BEHAVIORS;

        static {
            // Forced to do this try/catch otherwise won't compile.
            // Good practice I suppose, but annoying.
            try {
                BUILDINGS = registerSimple(KEYS.BUILDINGS, (reg) -> null);
                COMPONENTS = registerSimple(KEYS.COMPONENTS, (reg) -> ModComponents.GENERIC.DUMMY);
                MIND_BEHAVIORS = registerSimple(KEYS.MIND_BEHAVIORS, (reg) -> ModBehaviors.DUMMY);
            } catch (Exception e) {
                throw new IllegalStateException("(MiAlliance) Cannot create the Mod Registries!");
            }
        }


        @SuppressWarnings("unchecked")
        private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> key, RegistryBootstrap<T> bootstrap) throws InvocationTargetException, IllegalAccessException {
            Method method = Arrays.stream(BuiltinRegistries.class.getDeclaredMethods()).filter(methodCheck ->
                methodCheck.getParameterCount() == 2 && methodCheck.getName().equals("registerSimple")
            ).findFirst().orElseThrow();
            method.trySetAccessible();
            return (Registry<T>) method.invoke(key, bootstrap);
        }

        @FunctionalInterface
        public interface RegistryBootstrap<T> {
            T run(Registry<T> registry);
        }
    }
}
