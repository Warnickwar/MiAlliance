package org.warnickwar.components.utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class RegistryUtils {

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> key, RegistryBootstrap<T> bootstrap) throws InvocationTargetException, IllegalAccessException {
        Method method = Arrays.stream(BuiltInRegistries.class.getDeclaredMethods()).filter(methodCheck -> {
            return methodCheck.getParameterCount() == 2 && methodCheck.getName().equals("registerSimple");
        }).findFirst().orElseThrow();
        method.trySetAccessible();
        return (Registry<T>) method.invoke(key, bootstrap);
    }

    @FunctionalInterface
    public interface RegistryBootstrap<T> {
        T run(Registry<T> registry);
    }

}
