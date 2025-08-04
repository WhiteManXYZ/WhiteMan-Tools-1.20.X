package net.whiteman.whitemantools.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.whitemantools.WhiteManToolsMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WhiteManToolsMod.MOD_ID);

    public static final RegistryObject<Item> ALGANIT = ITEMS.register("alganit",
            () -> new Item(new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> PURIFIED_ALGANIT = ITEMS.register("purified_alganit",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SAND_DUST = ITEMS.register("sand_dust",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
