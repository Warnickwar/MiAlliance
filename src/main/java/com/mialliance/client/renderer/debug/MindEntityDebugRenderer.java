package com.mialliance.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MindEntityDebugRenderer implements DebugRenderer.SimpleDebugRenderer {

    private static final double RENDER_RANGE = 30.0D;
    private static final float ACTION_TEXT_SCALE = 0.02F;
    private static final float GOAL_TEXT_SCALE = 0.03F;

    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int ORANGE = -23296;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;

    private final Minecraft instance;
    private final Map<UUID, MindInfo> mindInfoPerEntity;
    private UUID lastLookedAtUuid;

    public MindEntityDebugRenderer(Minecraft instance) {
        this.instance = instance;
        this.mindInfoPerEntity = new HashMap<>();
    }

    public void addOrUpdateMindInfo(MindInfo information) {
        this.mindInfoPerEntity.put(information.uuid, information);
    }

    public void removeMindInfo(int id) {
        this.mindInfoPerEntity.values().removeIf(info -> info.id == id);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, double camX, double camY, double camZ) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        this.clearRemovedEntities();
        this.doRender();
        assert this.instance.player != null;
        if (!this.instance.player.isSpectator()) {
            this.updateLastLookedAt();
        }
    }

    private void doRender() {
        mindInfoPerEntity.values().forEach(info -> {
            if (isPlayerCloseEnough(info)) this.renderMindInfo(info);
        });
    }

    private void renderMindInfo(MindInfo info) {
        int offset = 0;
        renderTextOverMob(info.pos, offset++, info.toString(), -1, 0.03F);
        // Render only 5 Actions max in the Stack
        //  Start backwards to properly render the top Action
        for (int i = 4; i >= 0; i--) {
            String val = "";
            try {
                if (i == 0) {
                    val = info.getCurrentAction();
                } else {
                    val = info.actionStack.get(i);
                }
            } catch (Exception ignored) {
                val = "{No Action}";
            }

            renderTextOverMob(info.pos, offset++, val, i==0?ORANGE:WHITE, ACTION_TEXT_SCALE);
        }
        boolean hasGoal = !info.currentGoal.isEmpty();
        renderTextOverMob(info.pos, offset, hasGoal?info.currentGoal:"{No Goal}", hasGoal?GREEN:RED, GOAL_TEXT_SCALE);
    }

    @Override
    public void clear() {
        this.mindInfoPerEntity.clear();
    }

    private void clearRemovedEntities() {
        this.mindInfoPerEntity.entrySet().removeIf(entry -> {
            assert this.instance.level != null;
            Entity ent = this.instance.level.getEntity(entry.getValue().id);
            boolean isDying = ent instanceof LivingEntity lEnt && lEnt.isDeadOrDying();
            return ent == null || isDying;
        });
    }

    private boolean isEntitySelected(MindInfo info) {
        return this.lastLookedAtUuid == info.uuid;
    }

    private boolean isPlayerCloseEnough(MindInfo info) {
        Player player = this.instance.player;
        assert player != null;
        BlockPos playerPos = new BlockPos(player.getX(), info.pos.y(), player.getZ());
        return playerPos.closerThan(new BlockPos(info.pos), RENDER_RANGE);
    }

    private void updateLastLookedAt() {
        DebugRenderer.getTargetedEntity(this.instance.getCameraEntity(), 8).ifPresent((ent) -> {
            this.lastLookedAtUuid = ent.getUUID();
        });
    }

    private void renderTextOverMob(Position position, int offset, String text, int color, float scale) {
        double posOff = ((double) text.length() / 2.0D)*0.05D;
        DebugRenderer.renderFloatingText(text, position.x(), position.y()+ 2.4D + (double)offset*0.25D, position.z(), color, scale, false, 0.5F, true);
    }

    public static class MindInfo {

        public final ResourceLocation entityType;
        public final UUID uuid;
        public final int id;
        public final Position pos;
        public final List<String> actionStack;
        public final String currentGoal;

        public MindInfo(ResourceLocation entityType, UUID uuid, int id, Position pos, List<String> actionStack, String currentGoal) {
            this.entityType = entityType;
            this.uuid = uuid;
            this.id = id;
            this.pos = pos;
            this.actionStack = actionStack;
            this.currentGoal = currentGoal;
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String toString() {
            return this.entityType.toString();
        }

        public String getCurrentAction() {
            return !actionStack.isEmpty() ? actionStack.get(0) : "{No Action}";
        }
    }

}
