package org.warnickwar.components.functional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.warnickwar.components.functional.events.ComponentEvent;
import org.warnickwar.components.functional.annotations.EventListener;
import org.warnickwar.components.functional.events.ToggledComponentEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class Component<T extends ComponentHandler<?>> {

    private final ComponentType<?,T> type;

    protected final T handler;

    boolean disabled;
    boolean hasFirstTicked;

    // Used to write only changed Components to synchronize
    private boolean dirty;

    public Component(@NotNull ComponentType<?,T> type, @NotNull T handler) {
        this.type = type;
        this.handler = handler;

        this.disabled = false;
        this.hasFirstTicked = false;
        this.dirty = false;
    }

    // <---> DEFAULT FUNCTIONS <--->

    public abstract void start();

    public abstract void tick();

    public void firstTick() {}

    public void removed() {}

    public void setDisabled(Boolean disabledState) {
        // Maybe fire a native event for Disabling of a ComponentType?
        // Could be a good example of an ComponentEvent.
        if (this.disabled == disabledState) return;
        this.disabled = disabledState;
        handler.emit(new ToggledComponentEvent(type, disabledState));
    }

    // <---> ACCESSOR FUNCTIONS <--->

    public <E extends ComponentEvent> Map<Class<E>, Consumer<E>> getEventFunctions() {
        return this.getEventListenersByAnnotation();
    }

    public T getHandler() {
        return this.handler;
    }

    public ComponentType<?,T> getType() {
        return type;
    }

    // <---> SERIALIZATION FUNCTIONS <--->

    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    public void load(CompoundTag tag) {}

    public FriendlyByteBuf write(FriendlyByteBuf buffer) {
        return buffer;
    }

    public void read(FriendlyByteBuf buffer) {}

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean hasFirstTicked() {
        return hasFirstTicked;
    }

    public boolean isDirty() {
        return dirty;
    }

    // <---> INTERNAL FUNCTIONS <--->

    protected <E extends ComponentEvent> Map<Class<E>, Consumer<E>> getEventListenersByAnnotation() {
        Stream<Method> methods = Arrays.stream(this.getClass().getDeclaredMethods()).filter(method -> method.isAnnotationPresent(EventListener.class));
        Map<Class<E>, Consumer<E>> finalMethods = new HashMap<>();
        for (Iterator<Method> it = methods.iterator(); it.hasNext();) {
            Method method = it.next();

            if (method.isDefault() || method.isBridge()) continue;

            if (method.getReturnType() != Void.TYPE) continue;

            if (method.getParameterCount() != 1) continue;

            EventListener anno = method.getAnnotation(EventListener.class);

            if (!method.getParameterTypes()[0].isAssignableFrom(anno.value())) continue;

            Consumer<E> cons = toConsumer(method);

            //noinspection unchecked
            finalMethods.put((Class<E>) anno.value(), cons);
        }
        return finalMethods;
    }

    // <---> DEFAULT FUNCTIONS <--->

    @Override
    public String toString() {
        ResourceLocation typeLoc = ComponentType.getKey(type);
        String res = typeLoc == null ? "missingComponentTypeID" : typeLoc.toString();
        return String.format("%s{hasFirstTicked:%s}", res, hasFirstTicked);
    }

    private static <T> Consumer<T> toConsumer(Method m) {
        return param -> {
            try {
                m.invoke(param);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
