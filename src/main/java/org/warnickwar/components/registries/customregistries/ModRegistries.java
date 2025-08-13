package org.warnickwar.components.registries.customregistries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.warnickwar.components.functional.ComponentType;
import org.warnickwar.components.registries.definitions.ModComponents;
import org.warnickwar.components.Constants;
import org.warnickwar.components.utils.RegistryUtils;

public final class ModRegistries {

    public static class KEYS {

        public static final ResourceKey<Registry<ComponentType<?,?>>> FUNCTIONAL_COMPONENT_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Constants.MODID, "functional_components"));

    }

    public static class REGISTRIES {
        // These warnings will fucking kill me. Ignore for now.
        public static final Registry<ComponentType<?,?>> FUNCTIONAL_COMPONENT_REGISTRY;

        static {
            // Forced to do this try/catch otherwise won't compile.
            // Good practice I suppose, but annoying.
            try {
                FUNCTIONAL_COMPONENT_REGISTRY = RegistryUtils.registerSimple(KEYS.FUNCTIONAL_COMPONENT_KEY, registry -> ModComponents.EMPTY);
            } catch (Exception e) {
                throw new IllegalStateException("(ComponentsAPI) Cannot create the Mod Registries!");
            }
        }
    }
}
