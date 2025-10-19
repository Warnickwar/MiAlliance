package com.mialliance.components;

import com.mialliance.components.events.ComponentEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

public abstract class Component<T extends ComponentObject> {

    private boolean isDirty;

    @Nullable
    ComponentManager manager = null;

    public Component() {
        this.isDirty = false;
    }

    // <--- FUNCTIONALITY --->

    protected void onAdd() {}

    protected void onEnable() {
        this.setDirty(true);
    }

    final void tick() {
        // Just to spite god, I may mixin my own check to dedicate whether
        //  A logical server WILL exist, without guessing that Forge does.
        if (EffectiveSide.get().isServer()) {
            serverTick();
        } else {
            clientTick();
        }
    }

    protected void serverTick() {}

    protected void clientTick() {}

    protected void onDisable() {
        this.setDirty(true);
    }

    protected void onRemove() {}

    // <--- ACCESSING --->

    /**
     * <p>
     *      Gets this Component's associated Manager. This can be used to affect things outside
     *      This Component.
     * </p>
     * @return The associated {@link ComponentManager} which this Component is attached to.
     * @throws IllegalStateException If the Component has not been assigned a Manager yet.
     * @implNote Do not use this function in the Component's Constructor.
     */
    public final ComponentManager getManager() {
        if (this.manager == null) {
            throw new IllegalStateException("Cannot have a component without a Manager!");
        } else {
            return this.manager;
        }
    }

    @Nullable
    public final <C extends Component<?>> C getComponent(ComponentType<C, ?> type) {
        return this.getManager().getComponent(type);
    }

    // <--- EVENTS --->

    protected <E extends ComponentEvent, C extends Consumer<E>> Set<Pair<Class<E>, C>> registerEvents() {
        return Set.of();
    }

    protected final <E extends ComponentEvent> void emitEvent(E event) {
        this.getManager().emitEvent(event);
    }

    // <--- SERIALIZATION --->

    public void load(CompoundTag tag) {}

    public void save(CompoundTag tag) {}

    // <--- NETWORKING --->

    void writeToNetwork(FriendlyByteBuf buffer) {}

    void readFromNetwork(FriendlyByteBuf buffer) {}

    protected void setDirty(boolean newValue) {
        this.isDirty = newValue;
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    // <--- RENDERING --->

    boolean shouldRender() {
        return false;
    }

    final void render(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (EffectiveSide.get().isClient()) this.onRender(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private void onRender(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {}

    // <--- STATIC EXTRAS --->

    // This doesn't necessarily need to be here, it's just for the clean
    //  Component.LoadPriority.
    public enum LoadPriority {
        HIGHEST,    // First
        HIGH,       // Second
        NORMAL,     // Third
        LOW,        // Fourth
        LOWEST      // Fifth
    }
}
