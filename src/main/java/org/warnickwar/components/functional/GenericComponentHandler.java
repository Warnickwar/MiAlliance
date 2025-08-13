package org.warnickwar.components.functional;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.warnickwar.components.Constants;
import org.warnickwar.components.functional.events.ComponentAddedEvent;
import org.warnickwar.components.functional.events.ComponentRemovedEvent;
import org.warnickwar.components.functional.events.ComponentEvent;
import org.warnickwar.components.utils.DebugUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

// TODO: Figure out how to make a S2C Synchronization Packet for Synchronizing to Entities.
public abstract class GenericComponentHandler<T> implements ComponentHandler<T>{

    private static final String COMPONENT_LIST_DATA = "components";
    private static final String LOCKED_DATA = "locked_components";
    private static final String COMPONENT_DATA = "data";
    private static final String TYPE_DATA = "component_type";

    private final T parent;

    protected final Map<ComponentType<?,?>, Component<?>> components = new HashMap<>();
    protected final Set<ComponentType<?,?>> lockedComponents = new HashSet<>();

    protected final Map<Class<ComponentEvent>, Map<ComponentType<?,?>, Consumer<ComponentEvent>>> eventListeners = new HashMap<>();

    public GenericComponentHandler(T parent) {
        this.parent = parent;
    }

    // <---> ACCESSOR FUNCTIONS <--->

    /**
     * Gets the "parent", or superior object of the GenericComponentHandler.
     * This is used to allow Components to access the superior.
     * @return The object which holds this GenericComponentHandler.
     */
    @Override
    public T getParent() {
        return parent;
    }

    /**
     * Attempts to get a Component based on type from the GenericComponentHandler.
     * @param type The Component Type to attempt to fetch.
     * @return The resulting Component, or {@link null} if no component is found.
     * @param <C> The Component Class.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <C extends Component<?>> C getComponent(ComponentType<C, ?> type) {
        return (C) components.get(type);
    }

    /**
     * Checks if the GenericComponentHandler has a Component in its list.
     * @param type The type to check.
     * @return true if present, false otherwise.
     */
    @Override
    public boolean hasComponent(ComponentType<?,?> type) {
        return components.containsKey(type);
    }

    /**
     * Checks if the GenericComponentHandler has the type locked.
     * @param type The type to check.
     * @return true if the Component is locked, false otherwise.
     */
    @Override
    public boolean isComponentLocked(ComponentType<?,?> type) {
        return lockedComponents.contains(type);
    }

    /**
     * Returns an immutable set of all Components on the GenericComponentHandler.
     * @return A set copy of the Components in this handler.
     */
    // Don't expose the actual HashMap, return an immutable set
    @Override
    public Set<Component<?>> getComponents() {
        return ImmutableSet.copyOf(components.values());
    }

    // <---> EVENT    FUNCTIONS <--->

    /**
     * Emits an Internal ComponentEvent based on the ComponentEvent Class.
     * @param event The ComponentEvent being emitted.
     * @param <E> The ComponentEvent Type being emitted.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <E extends ComponentEvent> void emit(E event) {
        Class<E> clazz = (Class<E>) event.getClass();

        Map<ComponentType<?,?>, Consumer<ComponentEvent>> typeMap = eventListeners.get(clazz);

        if (typeMap == null) return;

        Iterator<Consumer<ComponentEvent>> listeners = typeMap.values().iterator();
        Iterator<ComponentType<?,?>> componentTypes = typeMap.keySet().iterator();

        DebugUtils.logIfDebug(String.format("Emitting ComponentEvent %s for %s!", clazz.getSimpleName(), parent.toString()));

        while (listeners.hasNext()) {
            ComponentType<?,?> type = componentTypes.next();
            Consumer<ComponentEvent> listener = listeners.next();

            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                // Minimal Effect Error; Error in Editor, but not in-game
                // Suppress Error outside of IDE
                DebugUtils.errorInEditor(new IllegalArgumentException(String.format("Could not invoke ComponentEvent %s for Component %s!", clazz.getSimpleName(), ComponentType.getKey(type))));
            }
        }
    }

    // <---> MUTATION FUNCTIONS <--->
    // TODO: Documentation

    @Override
    public boolean lock(ComponentType<?,?> type) {
        return hasComponent(type) && lockedComponents.add(type);
    }

    @Override
    public boolean unlock(ComponentType<?,?> type) {
        return lockedComponents.remove(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addComponent(ComponentType<?,?> type) {
        return addComponentAndEmit((ComponentType<?, GenericComponentHandler<T>>) type, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addPermanentComponent(ComponentType<?,?> type) {
        return addComponentAndEmit((ComponentType<?, GenericComponentHandler<T>>) type, true);
    }

    @Override
    public boolean removeComponent(ComponentType<?,?> type) {
        if (isComponentLocked(type)) return false;
        Component<?> comp = components.remove(type);
        if (comp == null) return false;
        removeEventListeners(comp);
        comp.removed();
        this.emit(new ComponentRemovedEvent(type));
        return true;
    }

    // <---> SERIALIZATION FUNCTIONS <--->

    /**
     * Prepares to save the GenericComponentHandler, along with all of its components, in the case Serialization is necessary.
     * @param tag The CompoundTag where the GenericComponentHandler is intended to be saved.
     * @return The original tag, with all relevant information input.
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag componentList = new ListTag();
        ListTag lockedList = new ListTag();

        Set<Component<?>> components = getComponents();

        components.forEach(comp -> {
            CompoundTag saveData = new CompoundTag();
            ComponentType<?,?> type = comp.getType();
            StringTag typeTag = StringTag.valueOf(type.toString());

            saveData.put(TYPE_DATA, typeTag);
            saveData.put(COMPONENT_DATA, comp.save(new CompoundTag()));
            if (isComponentLocked(type)) lockedList.add(typeTag);
            componentList.add(saveData);
        });

        tag.put(COMPONENT_LIST_DATA, componentList);
        tag.put(LOCKED_DATA, lockedList);

        return tag;
    }

    /**
     * Loads a ByteBuffer off of a CompoundTag.
     * @param tag The tag which holds the GenericComponentHandler information.
     */
    @Override
    public void load(CompoundTag tag) {
       ListTag componentList = tag.getList(COMPONENT_LIST_DATA, ListTag.TAG_COMPOUND);
       ListTag lockedList = tag.getList(LOCKED_DATA, ListTag.TAG_STRING);

        for (Tag value : componentList) {
            CompoundTag compound = (CompoundTag) value;
            StringTag typeTag = (StringTag) compound.get(TYPE_DATA);

            if (typeTag == null) {
                DebugUtils.errorQuietly("Could not find Component Type data, discarding Functional Component.");
                continue;
            }

            String typeName = typeTag.getAsString();
            ComponentType<?, GenericComponentHandler<T>> type = ComponentType.getType(new ResourceLocation(typeName));

            if (type == null) {
                DebugUtils.errorQuietly(String.format("Could not find Component Type %s, discarding Functional Component.", typeName));
                continue;
            }

            Component<?> component = type.build(this);
            component.load(compound.getCompound(COMPONENT_DATA));

            components.put(type, component);

            // This is weird, but basically.
            // If ANY ResourceLocation matches the TypeName of the current component, lock the component,
            if (lockedList.stream().anyMatch(sTag -> {
                StringTag stringTag = (StringTag) sTag;

                return stringTag.getAsString().equals(typeName);
            })) {
                lockedComponents.add(type);
            }
        }
    }

    /**
     * Serializes the GenericComponentHandler, along with all its Components, to send to the Client.
     * @param buffer The Byte Buffer in which holds the data.
     * @return The used Byte Buffer.
     */
    @Override
    public FriendlyByteBuf write(FriendlyByteBuf buffer) {
        Set<Component<?>> components = getComponents();
        buffer.writeVarInt(components.size()); // Size of Handler Array
        for (Component<?> comp : components) {
            ResourceLocation type = ComponentType.getKey(comp.getType());
            if (type == null) continue; // Discard the Component, likely from removed mod
            buffer.writeUtf(type.toString());
            comp.write(buffer);
        }
        return buffer;
    }

    /**
     * Reads a Byte Buffer to load and unpack the information necessary for Synchronizing data.
     * @param buffer The Byte Buffer holding the GenericComponentHandler data.
     */
    @Override
    public void read(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            String typeName = buffer.readUtf();
            ComponentType<?,?> type = ComponentType.getType(new ResourceLocation(typeName));

            // Assume that the Type is a registered type- unregistered Components will never be shared.
            // If an unregistered component is shared, something went terribly wrong!
            assert type != null;

            Component<?> comp = getComponent(type);
            if (comp == null) {
                // Why would it be null;
                // Make it anyways ig
                addComponent(type);
                // Should work now that Component is instantiated
                comp = getComponent(type);
            }

            assert comp != null;

            comp.read(buffer);
        }
    }

    // <---> PROCESS FUNCTIONS <--->

    /**
     * Ticks and processes the FunctionalComponents of the Handler.
     * This should only ever be run on the SERVER side, not the CLIENT side.
     */
    @Override
    public void tick() {
        // Throw error if ticking on the Client side; Should only ever tick on the Server side.
        if (!Constants.isServerRunning) throw new IllegalStateException("(ComponentAPI) Functional Component Handlers should only ever be ticked on the SERVER Side!");
        // Tick components as needed
        getComponents().forEach(comp -> {
            if (!comp.disabled) {
                if (!comp.hasFirstTicked) comp.firstTick();
                comp.tick();
            }
        });
    }

    // <---> INTERNAL FUNCTIONS <--->

    /**
     * Adds and register's a Component's list of ComponentEvent Handlers
     * @param origin The Component for which to add the Listeners for
     */
    private void addNewEventListeners(Component<?> origin) {
        Map<Class<ComponentEvent>, Consumer<ComponentEvent>> listeners = origin.getEventFunctions();

        listeners.forEach((clazz, listener) -> {
            // Add the ComponentEvent class, if not already inside the map.
            if (!eventListeners.containsKey(clazz)) {
                eventListeners.put(clazz, new HashMap<>());
            }

            // Add the Component to the ComponentEvent Map
            eventListeners.get(clazz).put(origin.getType(), listener);
        });
    }

    /**
     * Removes the listeners for a Component. Usually done when removing a component from the list.
     * @param origin The original Component that is queued to be removed.
     */
    private void removeEventListeners(Component<?> origin) {
        Set<Class<ComponentEvent>> events = origin.getEventFunctions().keySet();
        ComponentType<?,?> type = origin.getType();

        events.forEach(clazz -> {
            Map<ComponentType<?,?>, Consumer<ComponentEvent>> result = eventListeners.get(clazz);
            if (result != null) {
                result.remove(type);
                if (result.isEmpty()) {
                    eventListeners.remove(clazz);
                }
            }
        });
    }

    private boolean addComponentAndEmit(ComponentType<?, GenericComponentHandler<T>> type, boolean locked) {
        if (hasComponent(type)) return false;

        // Builds and registers resulting Component
        Component<?> comp;
        components.put(type, (comp = type.build(this)));
        addNewEventListeners(comp);
        comp.start();

        // Locks Component if necessary
        if (locked) lock(type);

        this.emit(new ComponentAddedEvent(type, locked));

        return true;
    }

    // <---> DEFAULT FUNCTIONS <--->

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("GenericComponentHandler{");
        components.values().forEach(comp -> {
            builder.append(comp).append(",");
        });
        return builder.append("}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GenericComponentHandler<?> that = (GenericComponentHandler<?>) o;
        return Objects.equals(parent, that.parent) && Objects.equals(components, that.components) && Objects.equals(lockedComponents, that.lockedComponents) && Objects.equals(eventListeners, that.eventListeners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, components, lockedComponents, eventListeners);
    }

}