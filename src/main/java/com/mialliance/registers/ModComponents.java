package com.mialliance.registers;

import com.mialliance.MiAlliance;
import com.mialliance.ModRegistries;
import com.mialliance.components.Component;
import com.mialliance.components.ComponentObject;
import com.mialliance.components.ComponentType;
import com.mialliance.components.implementations.CooldownComponent;
import com.mialliance.components.implementations.MemoryComponent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class ModComponents {

    public static class ENTITY {

    }

    public static class COLONY {

    }

    public static class GENERIC {

        public static final ComponentType<Component<ComponentObject>, ComponentObject> DUMMY = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "dummy"), ComponentObject.class, () -> new Component<>(){});

        public static final ComponentType<MemoryComponent, ComponentObject> MEMORY_COMPONENT = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "memories"), ComponentObject.class, MemoryComponent::new);

        public static final ComponentType<CooldownComponent, ComponentObject> COOLDOWN_COMPONENT = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "cooldown"), ComponentObject.class, CooldownComponent::new);

    }

    private static <O extends ComponentObject, C extends Component<O>> ComponentType<C, O> register(ResourceLocation id, Class<O> validClass, ComponentType.ComponentFactory<C, O> factory) {
        return Registry.register(ModRegistries.REGISTRIES.COMPONENTS, id, new ComponentType<>(validClass, factory));
    }

    private static <O extends ComponentObject, C extends Component<O>> ComponentType<C, O> register(ResourceLocation id, Class<O> validClass, ComponentType.ComponentFactory<C, O> factory, ComponentType<?, O>... dependencies) {
        return Registry.register(ModRegistries.REGISTRIES.COMPONENTS, id, new ComponentType<>(validClass, factory, dependencies));
    }
}
