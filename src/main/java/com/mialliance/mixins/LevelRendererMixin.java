package com.mialliance.mixins;

import com.mialliance.Constants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Final
    @Shadow
    private RenderBuffers renderBuffers;

    @Inject(method="renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V", shift = At.Shift.AFTER))
    public void mialliance$render(PoseStack poseStack, float p_109601_, long p_109602_, boolean p_109603_, Camera camera, GameRenderer p_109605_, LightTexture p_109606_, Matrix4f p_109607_, CallbackInfo ci) {
        if (Constants.CLIENT.DEBUG_RENDERERS != null) {
            Constants.CLIENT.DEBUG_RENDERERS.render(poseStack, renderBuffers.bufferSource(), camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        }
    }
}
