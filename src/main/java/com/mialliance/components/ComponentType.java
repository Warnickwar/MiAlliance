package com.mialliance.components;

import com.google.common.collect.ImmutableList;
import com.mialliance.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ComponentType<C extends Component<O>, O extends ComponentObject> {

    private final Class<O> valid;
    private final ComponentFactory<C, O> factory;
    // What ComponentTypes this type subscribes to, or requires
    private final ComponentType<?, O>[] dependencies;
    // What ComponentTypes is subscribed to this type
    private final List<ComponentType<?, O>> dependants;

    @SuppressWarnings("unchecked")
    public ComponentType(Class<O> validClass, ComponentFactory<C, O> factory) {
        this(validClass, factory, new ComponentType[]{});
    }

    public ComponentType(Class<O> validClass, ComponentFactory<C, O> factory, ComponentType<?, O>[] dependencies) {
        this.valid = validClass;
        this.factory = factory;
        this.dependencies = dependencies;
        for (ComponentType<?, O> dependant : dependencies) {
            // Subscribe to all dependant ComponentTypes
            dependant.dependants.add(this);
        }
        this.dependants = new ArrayList<>();
    }

    public <U extends ComponentObject> boolean canAccept(@NotNull U unknown) {
        return this.valid.isInstance(unknown);
    }

    public boolean fulfillsDependencies(O object) {
        return dependencies.length == 0 || allMatch(object.getManager(), dependencies);
    }

    Collection<ComponentType<?, O>> getDependents() {
        return ImmutableList.copyOf(this.dependants);
    }

    @Nullable
    public C safeBuild(ComponentObject obj) {
        if (this.canAccept(obj)) {
            C comp = factory.buildEmpty();
            obj.getManager();
            return comp;
        }
        return null;
    }

    // Only use this when already checking if the type can accept!
    C unsafeBuild() {
        return factory.buildEmpty();
    }

    public static ResourceLocation getKey(ComponentType<?, ?> type) {
        return ModRegistries.REGISTRIES.COMPONENTS.getKey(type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <C extends Component<O>, O extends ComponentObject> ComponentType<C,O> getValue(ResourceLocation location) {
        return (ComponentType<C, O>) ModRegistries.REGISTRIES.COMPONENTS.get(location);
    }

    private static <O extends ComponentObject> boolean allMatch(ComponentManager<O> manager, ComponentType<?, O>[] dependencies) {
        for (ComponentType<?, O> type : dependencies) {
            if (!manager.hasComponent(type)) return false;
        }
        return true;
    }

    public interface ComponentFactory<C extends Component<O>, O extends ComponentObject> {
        C buildEmpty();
    }
}
