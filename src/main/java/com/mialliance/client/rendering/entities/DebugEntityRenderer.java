package com.mialliance.client.rendering.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class DebugEntityRenderer<T extends Entity> extends EntityRenderer<T> {

    protected DebugEntityRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(@NotNull T ent, float yaw, float partialTick, @NotNull PoseStack stack, @NotNull MultiBufferSource buffer, int packedLight) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f inverted = camera.getLookVector().copy();
        inverted.mul(-1);
        stack.mulPose(inverted.rotation());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T ent) {
        return MissingTextureAtlasSprite.getLocation();
    }

}
