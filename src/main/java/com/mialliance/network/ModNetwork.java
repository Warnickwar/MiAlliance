package com.mialliance.network;

import com.mialliance.MiAlliance;
import com.mialliance.network.packets.MindEntityS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public class ModNetwork {

    private static int packetId = 0;

    private static final ResourceLocation NETWORK_NAME = ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "network");
    private static final String PROTOCOL_VERSION = "1.0";
    private static final List<String> CLIENT_VERSIONS = List.of(PROTOCOL_VERSION);
    private static final List<String> SERVER_VERSIONS = List.of(PROTOCOL_VERSION);

    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        NETWORK_NAME,
        () -> PROTOCOL_VERSION,
        CLIENT_VERSIONS::contains,
        SERVER_VERSIONS::contains
    );

    private static int id() {
        return packetId++;
    }

    // A fucking hack of a solution to the fact that the class doesn't get loaded until referred to
    public static void load() {
        INSTANCE.messageBuilder(MindEntityS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
            .decoder(MindEntityS2CPacket::new)
            .encoder(MindEntityS2CPacket::write)
            .consumerMainThread(MindEntityS2CPacket::handle)
            .add();
    }

    @OnlyIn(Dist.CLIENT)
    public static class CLIENT_TO_SERVER {

        public static <P> void sendToServer(P packet) {
            INSTANCE.sendToServer(packet);
        }

    }

    public static class SERVER_TO_CLIENT {

        public static <P> void sendToClient(P packet, ServerPlayer player) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }

        public static <P> void sendToAllClients(P packet) {
            INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        }

        public static <P> void sendToTrackedChunk(P packet, LevelChunk chunk) {
            INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
        }

    }
}
