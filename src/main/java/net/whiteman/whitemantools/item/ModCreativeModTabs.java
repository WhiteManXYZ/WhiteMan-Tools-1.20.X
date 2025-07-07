package net.whiteman.whitemantools.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.whitemantools.WhiteManToolsMod;
import net.whiteman.whitemantools.block.ModBlocks;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WhiteManToolsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WHITEMAN_TOOLS_TAB = CREATIVE_MODE_TABS.register("whiteman_tools_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.UNKNOWN_COMPOUND.get()))
                    .title(Component.translatable("creativetab.whiteman_tools_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.UNKNOWN_COMPOUND.get());

                        pOutput.accept(ModBlocks.UV_LAMP_BLOCK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
