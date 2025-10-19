package com.mialliance.registers;

import com.mialliance.MiAlliance;
import com.mialliance.client.renderer.entity.TestEntityRenderer;
import com.mialliance.entity.TestEntity;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// I hate the fact I have to use Forge just as much as the next person. Go next.
public class ModEntities {

    private static final DeferredRegister<EntityType<?>> ENTITY_REGISTRY = DeferredRegister.create(Registry.ENTITY_TYPE_REGISTRY, MiAlliance.MODID);

    public static final RegistryObject<EntityType<TestEntity>> TEST_ENTITY = ENTITY_REGISTRY.register("testentity", () -> EntityType.Builder.of(TestEntity::new, MobCategory.MONSTER)
        .sized(1F, 1F)
        .build("testentity"));

    public static void register(IEventBus bus) {
        ENTITY_REGISTRY.register(bus);
    }

    // Really don't like using OnlyIn, but whatever-too tired to change.
    @Mod.EventBusSubscriber(modid = MiAlliance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RENDERERS {

        @SubscribeEvent
        public static void FMLCLientSetup(FMLClientSetupEvent evt) {
            EntityRenderers.register(TEST_ENTITY.get(), TestEntityRenderer::new);
        }

    }

    @Mod.EventBusSubscriber(modid = MiAlliance.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ATTRIBUTES {

        @SubscribeEvent
        public static void onAttributeCreation(EntityAttributeCreationEvent evt) {
            evt.put(TEST_ENTITY.get(), TestEntity.createDefaultAttributes());
        }
    }

}
