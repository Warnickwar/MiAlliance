package com.mialliance.network.packets;

import com.mialliance.MiAllianceConstants;
import com.mialliance.client.renderer.debug.MindEntityDebugRenderer;
import net.minecraft.core.PositionImpl;

public class ClientPacketHandler {

    public static void handleMindEntityDebugPacket(MindEntityS2CPacket packet) {
        MindEntityDebugRenderer.MindInfo info = new MindEntityDebugRenderer.MindInfo(packet.entityType, packet.uuid, packet.id, new PositionImpl(packet.x,packet.y,packet.z), packet.actionNames, packet.currentGoal);
        assert MiAllianceConstants.CLIENT.DEBUG_RENDERERS != null;
        MiAllianceConstants.CLIENT.DEBUG_RENDERERS.MIND_DEBUG.addOrUpdateMindInfo(info);
    }
}
