package net.whiteman.biosanity.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.custom.PurificationStationBlock;
import net.whiteman.biosanity.block.custom.RedstoneLampUVBlock;
import net.whiteman.biosanity.block.custom.neoplasm.NeoplasmCoreBlock;
import net.whiteman.biosanity.block.custom.neoplasm.NeoplasmRotBlock;
import net.whiteman.biosanity.block.custom.neoplasm.NeoplasmVeinBlock;
import net.whiteman.biosanity.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BiosanityMod.MOD_ID);

    public static final RegistryObject<Block> UV_LAMP_BLOCK = registerBlock("uv_lamp_block",
            () -> new RedstoneLampUVBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_LAMP)
                    .strength(1.2F)
            ));

    public static final RegistryObject<Block> PURIFICATION_STATION_BLOCK = registerBlock("purification_station_block",
            () -> new PurificationStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
            ));

    public static final RegistryObject<Block> NEOPLASM_BLOCK = registerBlock("neoplasm_block",
            () -> new NeoplasmCoreBlock(BlockBehaviour.Properties.of()
                    .strength(0.3f, 9f)
                    .randomTicks()
            ));

    public static final RegistryObject<Block> NEOPLASM_ROT_BLOCK = registerBlock("neoplasm_rot_block",
            () -> new NeoplasmRotBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f, 2f)
                    .randomTicks()
            ));

    public static final RegistryObject<Block> NEOPLASM_VEIN_BLOCK = registerBlock("neoplasm_vein_block",
            () -> new NeoplasmVeinBlock(BlockBehaviour.Properties.of()
                    .strength(4.5f, 6f)
                    .randomTicks()
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
