package net.whiteman.whitemantools.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.whitemantools.WhiteManToolsMod;
import net.whiteman.whitemantools.block.custom.CrystalizingStationBlock;
import net.whiteman.whitemantools.block.custom.NeoplasmCoreBlock;
import net.whiteman.whitemantools.block.custom.RedstoneLampUVBlock;
import net.whiteman.whitemantools.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, WhiteManToolsMod.MOD_ID);


    public static final RegistryObject<Block> NEOPLASM_BLOCK = registerBlock("neoplasm_block",
            () -> new NeoplasmCoreBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
                    .destroyTime((float) 2.0)
                    .randomTicks()
            ));

    public static final RegistryObject<Block> CRYSTALIZING_STATION = registerBlock("crystalizing_station_block",
            () -> new CrystalizingStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
            ));

    public static final RegistryObject<Block> UV_LAMP_BLOCK = registerBlock("uv_lamp_block",
            () -> new RedstoneLampUVBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP)
                    .destroyTime((float) 1.5)
                    ));

    public static final RegistryObject<Block> NETHER_ALGANIT_ORE = registerBlock("nether_alganit_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)
                    .strength(6f).requiresCorrectToolForDrops(), UniformInt.of(2, 5)));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> supplier) {
        RegistryObject<T> toReturn = BLOCKS.register(name, supplier);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
