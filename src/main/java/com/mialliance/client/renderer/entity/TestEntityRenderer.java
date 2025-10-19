package com.mialliance.client.renderer.entity;

import com.mialliance.MiAlliance;
import com.mialliance.entity.TestEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TestEntityRenderer extends EntityRenderer<TestEntity> {

    private static final ResourceLocation NULL = ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "");
    private final EntityRendererProvider.Context ctx;

    public TestEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    @Override
    public void render(TestEntity ent, float yaw, float pTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-ent.getBbWidth()/2, 0, -ent.getBbWidth()/2);
        BlockState res = ent.isDeadOrDying() ? Blocks.REDSTONE_BLOCK.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(res, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
//        renderText(ent, ent.getEntityData().get(TamableMindComponentEntity.DATA_CURRENT_ACTION).orElse(Component.literal("No Action")), poseStack, buffer, packedLight, 0.75F);
//        renderText(ent, Component.literal("Goal : " + ent.getEntityData().get(TamableMindComponentEntity.DATA_CURRENT_GOAL).orElse(Component.literal("No Goal")).getString()), poseStack, buffer, packedLight, 1.25F);
        super.render(ent, yaw, pTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TestEntity ent) {
        return NULL;
    }

    protected void renderText(Entity ent, Component component, PoseStack stack, MultiBufferSource buffer, int packedLight, float height) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(ent);
        float f = ent.getBbHeight() + height;
        stack.pushPose();
        stack.translate(0.0D, (double)f, 0.0D);
        stack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        stack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = stack.last().pose();
        float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f1 * 255.0F) << 24;
        Font font = this.getFont();
        float f2 = (float)(-font.width(component) / 2);
        font.drawInBatch(component, f2, 0, 553648127, false, matrix4f, buffer, false, j, packedLight);
        font.drawInBatch(component, f2, 0, -1, false, matrix4f, buffer, false, j, packedLight);
        stack.popPose();
    }

}
