package com.mialliance;

import com.mialliance.network.ModNetwork;
import com.mialliance.registers.ModEntities;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MiAlliance.MODID)
public class MiAlliance {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "mialliance";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Can't be final due to MiAlliance being an instance
    // Pain
    public static IEventBus eventBus;

    public MiAlliance(FMLJavaModLoadingContext ctx) {
        eventBus = ctx.getModEventBus();

        // Register ourselves for server and other game events we are interested in
        // Ew, Forge
        // - Warnickwar
        MinecraftForge.EVENT_BUS.register(this);
        ModEntities.register(eventBus);
        ModNetwork.load();
    }

}
