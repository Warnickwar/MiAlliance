package com.mialliance.mixins;

import com.mialliance.Constants;
import com.mialliance.client.renderer.debug.ModDebugRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// We want the mod debug renderers to be synchronized with Vanilla
//  So to avoid more complicated processing.
@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    public void mialliance$init(Minecraft mc, CallbackInfo ci) {
        Constants.CLIENT.DEBUG_RENDERERS = new ModDebugRenderers(mc);
    }

    @Inject(method = "clear", at = @At("TAIL"))
    public void mialliance$clear(CallbackInfo ci) {
        assert Constants.CLIENT.DEBUG_RENDERERS != null;
        Constants.CLIENT.DEBUG_RENDERERS.clear();
    }
}
