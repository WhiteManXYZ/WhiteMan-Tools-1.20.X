package net.whiteman.biosanity.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.ModBlocks;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BiosanityMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> biosanity_TAB = CREATIVE_MODE_TABS.register("biosanity_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ALGANIT.get()))
                    .title(Component.translatable("creativetab.biosanity_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.ALGANIT.get());
                        pOutput.accept(ModItems.PURIFIED_ALGANIT.get());
                        pOutput.accept(ModItems.SAND_DUST.get());

                        pOutput.accept(ModBlocks.UV_LAMP_BLOCK.get());
                        pOutput.accept(ModBlocks.NETHER_ALGANIT_ORE.get());
                        pOutput.accept(ModBlocks.PURIFICATION_STATION_BLOCK.get());
                        pOutput.accept(ModBlocks.NEOPLASM_BLOCK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
