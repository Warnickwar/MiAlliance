package com.mialliance;

import com.mialliance.client.renderer.debug.ModDebugRenderers;
import com.mialliance.entity.base.AbstractMi;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

public class Constants {

    public static boolean DEBUG = !FMLLoader.isProduction();

    // Use -256 Y to avoid any collisions with 0, 0, 0 in the actual World
    public static final Vec3 NULL_LOCATION = new Vec3(0, -256, 0);
    public static final BlockPos NULL_BLOCKPOS = new BlockPos(0, -256, 0);

    public static int FRIENDLY_OFFICER_PARTY_MAXIMUM = 8;

    public static double getAttackReachSqrt(LivingEntity source, LivingEntity target) {
        return (source.getBbWidth() * 2.0F * source.getBbWidth() * 2.0F) + target.getBbWidth();
    }

    public static boolean withinMeleeRange(LivingEntity source, LivingEntity target) {
        return source.distanceToSqr(target) <= Constants.getAttackReachSqrt(source, target);
    }

    public static int adjustedTickDelay(int currentTickDelay) {
        return Mth.positiveCeilDiv(currentTickDelay, 2);
    }

    public static ResourceLocation makeLocation(String name) {
        return ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, name);
    }

    public static String makeLocationCaptial(String name) {
        return MiAlliance.MODID + ':' +name;
    }

    public static AbstractMi getMi(int entityID, Level level) {
        Entity foundEnt = level.getEntity(entityID);
        if (foundEnt == null) return null;
        return foundEnt instanceof AbstractMi mi ? mi : null;
    }

    public static class LOCATIONS {

        public static final ResourceLocation NOTHING_GOAL = makeLocation("idle");
        public static final String FOLLOW_OFFICER = makeLocationCaptial("followOfficer");
    }
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
