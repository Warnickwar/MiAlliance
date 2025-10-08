package com.mialliance.components;

import com.mialliance.components.events.ComponentEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class ComponentManager<O extends ComponentObject> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMPONENTS_KEY = "components";

    O owner;
    private final HashMap<ComponentType<?, O>, ComponentWrapper<?, O>> components;
    private final List<ComponentType<?, O>> permanentTypes;
    private final List<ComponentType<?, O>> removedTypes;
    private final HashMap<ComponentType<?, O>, StateToggles> toggledTypes;

    private final HashMap<Class<?>, List<EventListener<?>>> events;

    private boolean hasStarted;
    private final boolean shouldNetworkSynchronize;

    public ComponentManager(O owner, boolean shouldNetworkSynchronize) {
        this.owner = owner;
        this.components = new HashMap<>();
        this.permanentTypes = new ArrayList<>();
        this.removedTypes = new ArrayList<>();
        // Use a Hashmap such that multiple values for one type do not get stored
        this.toggledTypes = new HashMap<>();

        this.events = new HashMap<>();

        this.hasStarted = false;
        this.shouldNetworkSynchronize = shouldNetworkSynchronize;
    }

    public ComponentManager(O owner) {
        // By Default, we should enable functionality to synchronize across networks.
        //  Disable to conserve memory and processing power.
        //  Disabling means that some Components may not synchronize properly!
        this(owner, true);
    }

    public O getOwner() {
        return this.owner;
    }

    // <--- ACCESSING --->

    public void addComponent(ComponentType<?, O> type, Component.LoadPriority priority) {
        this.add(type, priority, true);
    }

    public void addComponent(ComponentType<?, O> type) {
        this.addComponent(type, Component.LoadPriority.NORMAL);
    }

    public void addPermanentComponent(ComponentType<?, O> type, Component.LoadPriority priority) {
        if (this.add(type, priority, true)) {
            this.permanentTypes.add(type);
        }
    }

    public void addPermanentComponent(ComponentType<?, O> type) {
        this.addPermanentComponent(type, Component.LoadPriority.NORMAL);
    }

    public boolean removeComponent(ComponentType<?, O> type) {
        if (this.isComponentPermanent(type) || !this.hasComponent(type)) {
            return false;
        } else {
            ComponentWrapper<?, O> wrap = this.components.remove(type);
            assert wrap != null;
            // Remove Events that have been registered
            Set<Pair<Class<ComponentEvent>, Consumer<ComponentEvent>>> listeners = wrap.component.registerEvents();
            listeners.forEach(pair -> this.removeListener(type, pair.getFirst()));

            wrap.component.onRemove();
            if (this.shouldNetworkSynchronize) {
                this.removedTypes.add(type);
            }
            this.removeDependentComponents(type);
            return true;
        }
    }

    public boolean isComponentPermanent(ComponentType<?, O> type) {
        return this.permanentTypes.contains(type);
    }

    public boolean hasComponent(ComponentType<?, O> type) {
        return this.components.containsKey(type);
    }

    public void enableComponent(ComponentType<?, O> type) {
        ComponentWrapper<?, O> wrap = this.components.get(type);
        if (wrap != null && !wrap.isEnabled) {
            this.toggledTypes.put(type, StateToggles.ENABLE);
            wrap.enable();
        }
    }

    public void disableComponent(ComponentType<?, O> type) {
        ComponentWrapper<?, O> wrap = this.components.get(type);
        if (wrap != null && wrap.isEnabled) {
            this.toggledTypes.put(type, StateToggles.DISABLE);
            wrap.disable();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <C extends Component<?>> C getComponent(ComponentType<C, ?> type) {
        ComponentWrapper<C, ?> wrap = (ComponentWrapper<C, ?>) this.components.get(type);
        if (wrap == null) return null;
        return wrap.component;
    }

    // <--- FUNCTIONALITY --->

    public void start() {
        if (this.hasStarted) return;
        this.hasStarted = true;

        components.values().forEach(ComponentWrapper::enable);
    }

    public void tick() {
        components.values().forEach(comp -> {
            if (EffectiveSide.get().isServer()) {
                // Only tick Validated Components as a failsafe
                if (comp.isValid) {
                    // Don't need to check for logical side-Component handles that innately lol
                    if (comp.isEnabled) {
                        comp.tick();
                    }
                } else if (comp.hasCheckedValidation) {
                    // Remove Invalid Components IF they have been checked
                    //  and still fail the Valid check
                    components.remove(comp.type, comp);
                } else {
                    // Has not checked Validation, check it before anything else
                    comp.validate(this.owner);
                }
            } else {
                // Client assumes all is well and will tick appropriately.
                comp.tick();
            }
        });
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (EffectiveSide.get().isServer()) throw new IllegalStateException("Cannot render Components on the Server Side!");
        components.values().forEach(comp -> {
            // On Client, assume all is well with validity-Server handles and checks that for us!
            if (comp.isEnabled && comp.component.shouldRender()) comp.component.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        });
    }

    // <--- EVENTS --->

    @SuppressWarnings("unchecked")
    public <E extends ComponentEvent> void emitEvent(@NotNull E event) {
        List<EventListener<?>> listeners = this.events.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) return;

        listeners.forEach(listener -> ((EventListener<E>) listener).run(event));
    }

    // <--- SERIALIZATION --->

    public CompoundTag save() {
        CompoundTag data = new CompoundTag();
        ListTag comps = new ListTag();
        components.values().forEach(wrap -> {
            CompoundTag componentData = new CompoundTag();
            componentData.putBoolean("permanent", this.permanentTypes.contains(wrap.type));
            wrap.save(componentData);
            comps.add(componentData);
        });
        data.put(COMPONENTS_KEY, comps);
        return data;
    }

    public void load(CompoundTag source) {
        ListTag comps = source.getList(COMPONENTS_KEY, ListTag.TAG_COMPOUND);

        // Populate all valid Component Types
        comps.forEach(tag -> {
            if (!(tag instanceof CompoundTag compound)) return;
            ComponentType<Component<O>, O> type = ComponentType.getValue(ResourceLocation.parse(compound.getString(ComponentWrapper.TYPE_KEY)));

            // Discard removed Component Type
            if (type == null) return;

            // Discard illegal Component Type
            if (!type.canAccept(this.owner)) return;

            // If not initialized, make new Component with Type out of saved data
            //  Used so that there aren't duplicate Permanent components
            if (!this.hasComponent(type)) {
                Component.LoadPriority priority = Component.LoadPriority.values()[compound.getInt(ComponentWrapper.LOAD_PRIORITY_KEY)];
                Component<O> comp = type.unsafeBuild();
                ComponentWrapper<Component<O>, O> wrap = new ComponentWrapper<>(type, comp, priority);

                // Force Validation-we already checked this, and it should not change.
                //  If it did change, then confer with Thoro's 11th edition Black Magic.
                wrap.isValid = true;
                // Avoid inconsistent behavior
                wrap.hasCheckedValidation = true;
                // Synchronize all Components to the Client
                wrap.component.setDirty(true);
                wrap.load(compound);
                components.put(type, wrap);
            } else {
                ComponentWrapper<?, O> wrap = this.components.get(type);
                wrap.component.setDirty(true);
                // Don't put into Hashmap, already present by now; Simply load and continue.
                wrap.load(compound);
            }

        });

        // Initialize and load all Components
        PriorityQueue<ComponentWrapper<?, O>> orderedComponents = new PriorityQueue<>(Comparator.comparing(wrap -> wrap.loadPriority));
        orderedComponents.addAll(components.values());
        // Enable in respect to the Load Priority
        orderedComponents.forEach(ComponentWrapper::enable);
    }

    // <--- NETWORKING --->

    public FriendlyByteBuf writeToNetwork(FriendlyByteBuf buffer) {
        if (EffectiveSide.get().isClient()) throw new IllegalStateException("Cannot write to network on the logical CLIENT for sending to the SERVER!");

        // Write all DIRTY Components
        //  NEW Components that are valid are considered Dirty and thus auto-synchronized
        //
        // No need to write if a Component is Locked-The Client shouldn't care about that
        //  Nor should the Load Priority be cared about, as, again, it is server responsibility only.
        List<ComponentWrapper<?, O>> dirtyComps = components.values().stream().filter(ComponentWrapper::isDirty).toList();
        buffer.writeInt(dirtyComps.size());
        dirtyComps.forEach(wrap -> {
            wrap.component.writeToNetwork(buffer);
            // The Component is written, so it is no longer dirty
            wrap.component.setDirty(false);
        });

        // Write all DISABLED/ENABLED Components
        buffer.writeInt(this.toggledTypes.size());
        this.toggledTypes.forEach((type, newState) -> {
            buffer.writeResourceLocation(ComponentType.getKey(type));
            buffer.writeInt(newState.ordinal());
        });

        // Write all REMOVED Components
        buffer.writeInt(removedTypes.size());
        removedTypes.forEach(type -> {
            ResourceLocation loc = ComponentType.getKey(type);
            assert loc != null;
            buffer.writeResourceLocation(loc);
        });
        return buffer;
    }


    public void readFromNetwork(FriendlyByteBuf buffer) {
        if (EffectiveSide.get().isServer()) throw new IllegalStateException("Cannot read from network on the logical SERVER for updating data!");

        // Read all DIRTY Components
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            ResourceLocation loc = buffer.readResourceLocation();
            ComponentType<?, O> type = ComponentType.getValue(loc);
            if (type == null) throw new IllegalArgumentException("Cannot find ComponentType of " + loc + "! Are the Server and Client mod versions the same?");
            // Add new Component if not already present
            if (!this.hasComponent(type)) {
                if (!this.add(type, Component.LoadPriority.NORMAL, true)) throw new IllegalArgumentException("ComponentType of " + loc + " Cannot be applied to ComponentObject of " + this.owner.getClass() + "!");
            }
            ComponentWrapper<?, O> wrap = this.components.get(type);
            assert wrap != null;
            wrap.component.readFromNetwork(buffer);
        }

        // Read all ENABLED/DISABLED Components
        int toggledSize = buffer.readInt();
        for (int i = 0; i < toggledSize; i++) {
            ResourceLocation loc = buffer.readResourceLocation();
            StateToggles toggle = StateToggles.values()[buffer.readInt()];
            ComponentType<?, O> type = ComponentType.getValue(loc);
            if (type == null) throw new IllegalArgumentException("Cannot find ComponentType of " + loc + "! Are the Server and Client mod versions the same?");
            switch (toggle) {
                case ENABLE -> this.enableComponent(type);
                case DISABLE -> this.disableComponent(type);
            }
        }


        // Read all REMOVED Components
        int removedSize = buffer.readInt();
        for (int i = 0; i < removedSize; i++) {
            ResourceLocation loc = buffer.readResourceLocation();
            ComponentType<?, O> type = ComponentType.getValue(loc);
            if (type == null) throw new IllegalArgumentException("Cannot find ComponentType of " + loc + "! Are the Server and Client mod versions the same?");
            // Components should never be permanent on the Client Side
            if (this.hasComponent(type)) this.removeComponent(type);
        }
    }

    // <--- EXTRA UTILITIES --->

    private <C extends Component<O>> boolean add(@NotNull ComponentType<C, O> type, @NotNull Component.LoadPriority priority, boolean autoEnable) {
        if (!type.fulfillsDependencies(this.owner)) return false;
        C comp = type.safeBuild(this.owner);
        if (comp == null) return false;
        ComponentWrapper<C, O> wrap = new ComponentWrapper<>(type, comp, priority);
        this.components.put(type, wrap);
        // Assign Manager to this object so that the Component is aware of others
        wrap.component.manager = this;
        wrap.component.onAdd();
        // Validate as we used SafeBuild.
        wrap.isValid = true;
        wrap.hasCheckedValidation = true;

        // Register this Component for specific Events
        Set<Pair<Class<ComponentEvent>, Consumer<ComponentEvent>>> listeners = wrap.component.registerEvents();
        listeners.forEach((pair) -> {
            this.registerListener(type, pair.getFirst(), pair.getSecond());
        });


        if (autoEnable) {
            // With setup complete, enable the Component by default
            wrap.enable();
        }
        return true;
    }

    private <C extends Component<O>> void removeDependentComponents(@NotNull ComponentType<C, O> type) {
        type.getDependents().forEach(this::removeComponent);
    }

    private <C extends ComponentEvent, L extends Consumer<C>> void registerListener(ComponentType<?, ?> type, Class<C> clazz, L listener) {
        EventListener<C> listenerWrapper = new EventListener<>(type, listener);
        List<EventListener<?>> listeners = this.events.get(clazz);
        if (listeners == null) {
            listeners = new ArrayList<>();
            listeners.add(listenerWrapper);
            this.events.put(clazz, listeners);
        } else {
            listeners.add(listenerWrapper);
        }
    }

    private <C extends ComponentEvent> void removeListener(ComponentType<?, ?> type, Class<C> clazz) {
        List<EventListener<?>> listeners = this.events.get(clazz);
        if (listeners == null) return; // How???
        listeners.forEach(func -> {
            if (func.componentOwner == type) listeners.remove(func);
        });

        if (listeners.isEmpty()) {
            this.events.remove(clazz);
        }
    }

    private enum StateToggles {
        DISABLE,// 0
        ENABLE; // 1
    }

    static class ComponentWrapper<C extends Component<O>, O extends ComponentObject> {

        static final String LOAD_PRIORITY_KEY = "loadPriority";
        static final String TYPE_KEY = "type";
        static final String DATA_KEY = "data";

        private final Component.LoadPriority loadPriority;
        private final ComponentType<C, O> type;
        private final C component;

        private boolean isValid = false;
        private boolean hasCheckedValidation = false;
        private boolean isEnabled = false;

        ComponentWrapper(ComponentType<C, O> type, C component, Component.LoadPriority priority) {
            this.loadPriority = priority;
            this.type = type;
            this.component = component;
        }

        Component.LoadPriority getLoadPriority() {
            return this.loadPriority;
        }

        <U extends ComponentObject> void validate(U unknown) {
            this.hasCheckedValidation = true;
            if (this.type.canAccept(unknown)) this.isValid = true;
        }

        void tick() {
            this.component.tick();
        }

        void enable() {
            if (!isEnabled) {
                this.component.onEnable();
                isEnabled = true;
            }
        }

        void disable() {
            if (isEnabled) {
                this.component.onDisable();
                isEnabled = false;
            }
        }

        public void load(CompoundTag tag) {
            // Assume everything but the Component has been loaded due to the Constructor;
            //  If a Component that hasn't been registered gets to this point, WTF?
            this.component.load(tag.getCompound(DATA_KEY));
        }

        public void save(CompoundTag tag) {
            ResourceLocation loc = ComponentType.getKey(this.type);
            if (loc == null) {
                LOGGER.error("Could not find key for Component Type! Discarding Component...");
                return;
            }
            tag.putString(TYPE_KEY, loc.toString());
            tag.putInt(LOAD_PRIORITY_KEY, loadPriority.ordinal());
            CompoundTag data = new CompoundTag();
            // Serialize Component to its own CompoundTag to protect the data
            this.component.save(data);
            tag.put(DATA_KEY, data);
        }

        boolean isDirty() {
            return this.component.isDirty();
        }

    }

    static class EventListener<C extends ComponentEvent> {

        @NotNull
        ComponentType<?, ?> componentOwner;
        @NotNull
        Consumer<C> func;

        EventListener(@NotNull ComponentType<?, ?> componentOwner, @NotNull Consumer<C> func) {
            this.componentOwner = componentOwner;
            this.func = func;
        }

        void run(C event) {
            this.func.accept(event);
        }

    }

}
