package org.warnickwar.components.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.warnickwar.components.functional.ComponentHandler;
import org.warnickwar.components.functional.ComponentHolder;
import org.warnickwar.components.functional.ComponentType;
import org.warnickwar.components.functional.definitions.handlers.EntityComponentHandler;

public class ClientboundEntityComponentRemove implements Packet<ClientGamePacketListener> {

    private final int entId;

    private final ResourceLocation componentToRemove;

    public ClientboundEntityComponentRemove(Entity ent, ComponentType<?,?> type) {
        this.entId = ent.getId();
        this.componentToRemove = ComponentType.getKey(type);
    }

    public ClientboundEntityComponentRemove(@NotNull FriendlyByteBuf buffer) {
        this.entId = buffer.readVarInt();
        this.componentToRemove = buffer.readResourceLocation();
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVarInt(entId);
        buffer.writeResourceLocation(componentToRemove);
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

        // There should be no locked objects on the Client side, as that is only functional on the server
        ComponentType<?,?> type = ComponentType.getType(componentToRemove);
        assert type != null;
        cHandler.removeComponent(type);
    }

}
