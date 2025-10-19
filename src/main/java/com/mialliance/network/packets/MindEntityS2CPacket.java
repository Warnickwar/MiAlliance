package com.mialliance.network.packets;

import com.mialliance.mind.base.agent.EntityMindAgentHolder;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.plan.ActionPlan;
import com.mialliance.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Supplier;

public class MindEntityS2CPacket {

    private static final Logger LOGGER = LogUtils.getLogger();

    public final double x;
    public final double y;
    public final double z;
    public final UUID uuid;
    public final int id;
    public final LinkedList<String> actionNames;
    public final String currentGoal;
    public final ResourceLocation entityType;

    MindEntityS2CPacket(double x, double y, double z, UUID uuid, int id, LinkedList<String> currentActions, String currentGoal, ResourceLocation entityType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.uuid = uuid;
        this.id = id;
        this.actionNames = currentActions;
        this.currentGoal = currentGoal;
        this.entityType = entityType;
    }

    public MindEntityS2CPacket(FriendlyByteBuf buffer) {
        // Position
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        // UUID
        this.uuid = buffer.readUUID();
        // Identifier
        this.id = buffer.readInt();

        // Collect Action Names on the Stack
        this.actionNames = new LinkedList<>();
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            actionNames.add(buffer.readUtf());
        }
        // Goal
        this.currentGoal = buffer.readUtf();
        // EntityType
        this.entityType = buffer.readResourceLocation();
    }

    public void write(FriendlyByteBuf buffer) {
        // Position
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        // UUID
        buffer.writeUUID(this.uuid);
        // Identifier
        buffer.writeInt(this.id);
        // Actions
        buffer.writeVarInt(this.actionNames.size());
        this.actionNames.forEach(buffer::writeUtf);
        // Goal
        buffer.writeUtf(this.currentGoal);
        // Entity Type
        buffer.writeResourceLocation(this.entityType);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supp) {
        NetworkEvent.Context ctx = supp.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleMindEntityDebugPacket(this));
        });
        ctx.setPacketHandled(true);
        return true;
    }

    public static void sendMindInfo(EntityMindAgentHolder holder) {
        Entity ent = holder.getEntity();
        MindAgent<?> agent = holder.getAgent();
        LinkedList<String> actionPlan = new LinkedList<>();
        ActionPlan plan = agent.getCurrentPlan();
        if (agent.getCurrentAction() != null) {
            actionPlan.add(agent.getCurrentAction().getName());
        }
        if (plan != null) {
            plan.getActions().forEach(action -> {
                actionPlan.add(action.getName());
            });
        }
        String currentGoal = agent.getCurrentGoal() == null ? "" : agent.getCurrentGoal().getName();
        MindEntityS2CPacket packet = new MindEntityS2CPacket(ent.getX(), ent.getY(), ent.getZ(), ent.getUUID(), ent.getId(), actionPlan, currentGoal, EntityType.getKey(holder.getType()));

        // Only send to Players who have the chunk loaded
        LevelChunk chunk = ent.level.getChunkAt(ent.getOnPos());
        ModNetwork.SERVER_TO_CLIENT.sendToTrackedChunk(packet, chunk);
    }
}
