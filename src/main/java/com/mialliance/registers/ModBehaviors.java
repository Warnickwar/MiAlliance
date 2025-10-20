package com.mialliance.registers;

import com.mialliance.MiAlliance;
import com.mialliance.ModRegistries;
import com.mialliance.mind.base.agent.MindAgent;
import com.mialliance.mind.base.kits.Behavior;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class ModBehaviors {

    // Why can't I type you to MindAgent?
    //  Whatever, not a problem.
    public static final Behavior<?> DUMMY = register(ResourceLocation.fromNamespaceAndPath(MiAlliance.MODID, "dummy"),
        Behavior.start(MindAgent.class).build());

    private static <T extends MindAgent<?>> Behavior<T> register(ResourceLocation loc, Behavior<T> behavior) {
        Registry.register(ModRegistries.REGISTRIES.MIND_BEHAVIORS, loc, behavior);
        return behavior;
    }
}
