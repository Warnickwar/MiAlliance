package com.mialliance;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MiAlliance.MODID)
public class ModSubscriptions {


    // Yes, I hate this.
    //  I detest Forge, yet I do not have the time
    //  nor motivation to create a bypassable hack.
    //  Go Next.
    // - Warnickwar
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END) {
            while (KEYMAPPINGS.DEBUG_TOGGLE.consumeClick()) {
                MiAllianceConstants.DEBUG = !MiAllianceConstants.DEBUG;
                TextColor res = MiAllianceConstants.DEBUG ? TextColor.parseColor("#55FF55") : TextColor.parseColor("#FF5555");
                Minecraft.getInstance().gui.getChat().addMessage(Component.empty().append("[Debug]: ").append(Component.translatable("debug.mialliance.debug_toggle", MiAllianceConstants.DEBUG ? "Enabled" : "Disabled").withStyle(org -> org.withBold(true).withColor(res))));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MiAlliance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class KEYMAPPINGS {

        public static final KeyMapping DEBUG_TOGGLE = new KeyMapping(
            "key.mialliance.debug_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.mialliance.debug"
        );

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent evt) {
            evt.register(DEBUG_TOGGLE);
        }
    }
}
