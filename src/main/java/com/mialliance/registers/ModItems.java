package com.mialliance.registers;

import com.mialliance.MiAlliance;
import com.mialliance.items.BehaviorTestItem;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(Registry.ITEM_REGISTRY, MiAlliance.MODID);

    public static final RegistryObject<BehaviorTestItem> BEHAVIOR_TEST = REGISTRY.register("behaviortest", () -> new BehaviorTestItem(new Item.Properties(), ModBehaviors.TEST_BEHAVIOR));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
