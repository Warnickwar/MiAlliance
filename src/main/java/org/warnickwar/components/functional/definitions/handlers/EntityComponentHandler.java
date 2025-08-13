package org.warnickwar.components.functional.definitions.handlers;

import net.minecraft.network.FriendlyByteBuf;
import org.warnickwar.components.functional.Component;
import org.warnickwar.components.functional.GenericComponentHandler;
import org.warnickwar.components.functional.ComponentType;
import org.warnickwar.components.networking.ClientboundEntityComponentRemove;
import org.warnickwar.components.networking.ClientboundEntityComponentSynchronize;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityComponentHandler<T extends Entity> extends GenericComponentHandler<T> {

    public EntityComponentHandler(T parent) {
        super(parent);
    }

    @Override
    public void tick() {
        super.tick();
        AtomicBoolean bool = new AtomicBoolean(false);
        ArrayList<Component<?>> comps = new ArrayList<>();
        getComponents().forEach(comp -> {
            if (comp.isDirty()) {
                bool.set(true);
                comps.add(comp);
            }
        });
        if (!bool.get()) return;
        ClientboundEntityComponentSynchronize packet = new ClientboundEntityComponentSynchronize(getParent(), comps);
        // Launch packet over network to Players loading the Entity
    }

    @Override
    public boolean removeComponent(ComponentType<?,?> type) {
        boolean result = super.removeComponent(type);
        if (result) {
            ClientboundEntityComponentRemove packet = new ClientboundEntityComponentRemove(getParent(), type);
            // Launch packet over network to Players loading the entity
        }
        return result;
    }

    public ClientboundEntityComponentSynchronize getSyncPacket() {
        ClientboundEntityComponentSynchronize packet = new ClientboundEntityComponentSynchronize(getParent());
        ClientboundEntityComponentSynchronize.ServerView view = packet.getServerSidedView().orElseThrow(() -> new IllegalStateException("Cannot create Sync packet on the client!"));
        getComponents().forEach(comp -> {
            if (comp.isDirty()) {
                view.addComponent(comp);
            }
        });
        return packet;
    }

    public void acceptSyncPacket(ClientboundEntityComponentSynchronize packet) {
        ClientboundEntityComponentSynchronize.ClientView view = packet.getClientSidedView().orElseThrow(() -> new IllegalStateException("Why am I accepting client packets on the server?"));
        Queue<ComponentType<?,?>> types = view.getComponentsToUpdate();
        FriendlyByteBuf buffer = view.getBuffer();
        for (ComponentType<?,?> type : types) {
            Component<?> comp = getComponent(type);
            if (comp == null) {
                addComponent(type);
                comp = getComponent(type);
            }
            assert comp != null;
            comp.read(buffer);
        }
    }

}
