package net.whiteman.biosanity;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.whiteman.biosanity.client.gui.screens.inventory.PurificationStationScreen;
import net.whiteman.biosanity.client.resources.model.OverlayModelLoader;
import net.whiteman.biosanity.client.resources.sounds.ModSounds;
import net.whiteman.biosanity.message.ModMessages;
import net.whiteman.biosanity.world.inventory.ModMenuTypes;
import net.whiteman.biosanity.world.item.ModCreativeModTabs;
import net.whiteman.biosanity.world.item.ModItems;
import net.whiteman.biosanity.world.item.crafting.ModRecipes;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceRegistry;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BiosanityMod.MOD_ID)
public class BiosanityMod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "biosanity";
    // Directly reference a slf4j logger
    //private static final Logger LOGGER = LogUtils.getLogger();

    public BiosanityMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeModTabs.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        ModRecipes.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ResourceRegistry::setup);
        event.enqueueWork(ModMessages::register);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.ALGANIT);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MenuScreens.register(ModMenuTypes.PURIFICATION_STATION_BLOCK_MENU.get(), PurificationStationScreen::new);

            event.enqueueWork(() -> {
                // If block has overlays we add his here
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.NEOPLASM_ROT_BLOCK.get(), RenderType.translucent());
            });
        }

        @SubscribeEvent
        public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
            event.register("util_overlay", new OverlayModelLoader());
        }
    }
}
