package com.mialliance;

import com.mialliance.client.renderer.debug.ModDebugRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

public class MiAllianceConstants {

    public static boolean DEBUG = !FMLLoader.isProduction();

    // Use -256 Y to avoid any collisions with 0, 0, 0 in the actual World
    public static final Vec3 NULL_LOCATION = new Vec3(0, -256, 0);
    public static final BlockPos NULL_BLOCKPOS = new BlockPos(0, -256, 0);

    public static int FRIENDLY_OFFICER_PARTY_MAXIMUM = 8;

    public static class CLIENT {

        @Nullable
        public static ModDebugRenderers DEBUG_RENDERERS = null;

    }

    public static class SERVER {

        @Nullable
        public static MinecraftServer server;

        public static boolean isRunningLogicalServer() {
            return server != null;
        }

    }


}
