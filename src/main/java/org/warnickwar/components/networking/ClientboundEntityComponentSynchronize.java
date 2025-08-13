package org.warnickwar.components.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.warnickwar.components.functional.Component;
import org.warnickwar.components.functional.ComponentHandler;
import org.warnickwar.components.functional.ComponentHolder;
import org.warnickwar.components.functional.ComponentType;
import org.warnickwar.components.functional.definitions.handlers.EntityComponentHandler;

import java.util.*;

public class ClientboundEntityComponentSynchronize implements Packet<ClientGamePacketListener> {

    private final int entId;

    // AT SERVER
    private final List<Component<?>> componentsToSynchronize;

    // AT CLIENT
    final Queue<ComponentType<?,?>> componentsToDeserialize;
    final FriendlyByteBuf buffer;

    public ClientboundEntityComponentSynchronize(Entity ent) {
        this.entId = ent.getId();
        this.componentsToSynchronize = new LinkedList<>();
        componentsToDeserialize = new LinkedList<>();
        this.buffer = null;
    }

    public ClientboundEntityComponentSynchronize(Entity ent, Collection<Component<?>> components) {
        this.entId = ent.getId();
        this.componentsToSynchronize = new LinkedList<>(components);
        componentsToDeserialize = new LinkedList<>();
        this.buffer = null;
    }

    public ClientboundEntityComponentSynchronize(@NotNull FriendlyByteBuf buffer) {
        this.entId = buffer.readVarInt();
        int size = buffer.readVarInt();
        this.componentsToSynchronize = new LinkedList<>();
        componentsToDeserialize = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            componentsToDeserialize.add(Objects.requireNonNull(ComponentType.getType(buffer.readResourceLocation())));
        }
        // Don't deserialize the Component data yet - handle that when handing to the Handler.
        this.buffer = buffer;
    }



    @Override
    public void write(@NotNull FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(entId);
        friendlyByteBuf.writeVarInt(componentsToSynchronize.size());
        // Write all ComponentTypes first
        for (Component<?> component : componentsToSynchronize) {
            friendlyByteBuf.writeResourceLocation(Objects.requireNonNull(ComponentType.getKey(component.getType())));
        }
        // Write all Component serialization
        for (Component<?> component: componentsToSynchronize) {
            component.write(friendlyByteBuf);
        }
    }

    @Override
    public void handle(@NotNull ClientGamePacketListener handler) {
        Minecraft mc = Minecraft.getInstance();
        PacketUtils.ensureRunningOnSameThread(this, handler, mc);
        assert mc.level != null;
        Entity ent = mc.level.getEntity(this.entId);
        if (!(ent instanceof ComponentHolder<?>)) return;
        ComponentHandler<?> cHandler = ((ComponentHolder<?>) ent).getComponentHandler();
        if (!(cHandler instanceof EntityComponentHandler<?>)) return;
        ((EntityComponentHandler<?>) cHandler).acceptSyncPacket(this);
    }

    public Optional<ServerView> getServerSidedView() {
        return this.buffer != null ? Optional.empty() : Optional.of(new ServerView(this));
    }

    public Optional<ClientView> getClientSidedView() {
        // Buffer will only ever be filled out of an INCOMING creation
        return this.buffer == null ? Optional.empty() : Optional.of(new ClientView(this));
    }

    public static class ServerView {

        private final ClientboundEntityComponentSynchronize syncPacket;

        private ServerView(ClientboundEntityComponentSynchronize syncPacket) {
            this.syncPacket = syncPacket;
        }

        public void addComponent(Component<?> comp) {
            syncPacket.componentsToSynchronize.add(comp);
        }
    }

    // Used to allow for compile time safety of handling and accessing of the assets
    public static class ClientView {

        private final ClientboundEntityComponentSynchronize syncPacket;

        private ClientView(ClientboundEntityComponentSynchronize syncPacket) {
            this.syncPacket = syncPacket;
        }

        public Queue<ComponentType<?,?>> getComponentsToUpdate() {
            return syncPacket.componentsToDeserialize;
        }

        public FriendlyByteBuf getBuffer() {
            return syncPacket.buffer;
        }
    }

}
