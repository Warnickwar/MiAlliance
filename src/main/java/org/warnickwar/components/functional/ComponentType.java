package org.warnickwar.components.functional;

import net.minecraft.resources.ResourceLocation;
import org.warnickwar.components.registries.customregistries.ModRegistries;

import javax.annotation.Nullable;

public final class ComponentType<C extends Component<H>, H extends ComponentHandler<?>> {

    private final ComponentBuilder<H, C> builder;

    private ComponentType(ComponentBuilder<H, C> builder) {
        this.builder = builder;
    }

    /**
     * Returns a new Component Instance
     * @param handler The handler of which to parent to
     * @return a new instance of a Component
     */
    public C build(H handler) {
        return builder.build(this, handler);
    }

    @Nullable
    public static ResourceLocation getKey(ComponentType<?, ?> type) {
        return ModRegistries.REGISTRIES.FUNCTIONAL_COMPONENT_REGISTRY.getKey(type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <C extends Component<H>, H extends ComponentHandler<?>> ComponentType<C, H> getType(ResourceLocation loc) {
        return (ComponentType<C, H>) ModRegistries.REGISTRIES.FUNCTIONAL_COMPONENT_REGISTRY.get(loc);
    }

    public static <C extends Component<H>, H extends ComponentHandler<?>> ComponentType<C, H> of(ComponentBuilder<H, C> builder) {
        return new ComponentType<>(builder);
    }

    @Override
    public String toString() {
        ResourceLocation typeLoc = ComponentType.getKey(this);
        return typeLoc == null ? "missingComponentTypeID" : typeLoc.toString();
    }

    @FunctionalInterface
    public interface ComponentBuilder<H extends ComponentHandler<?>, C extends Component<H>> {
        C build(ComponentType<C,H> type, H handler);
    }
}
