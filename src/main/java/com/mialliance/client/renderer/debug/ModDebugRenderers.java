package com.mialliance.client.renderer.debug;

import com.mialliance.MiAllianceConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class ModDebugRenderers {

    public final MindEntityDebugRenderer MIND_DEBUG;

    private final Minecraft minecraft;

    public ModDebugRenderers(Minecraft mc) {
        this.minecraft = mc;
        this.MIND_DEBUG = new MindEntityDebugRenderer(mc);
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource buffer, double camX, double camY, double camZ) {
        if (!MiAllianceConstants.DEBUG) return;
        MIND_DEBUG.render(poseStack, buffer, camX, camY, camZ);
    }

    public void clear() {
        MIND_DEBUG.clear();
    }
}
