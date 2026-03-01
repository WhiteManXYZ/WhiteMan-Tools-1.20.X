package net.whiteman.biosanity.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.block.custom.RedstoneLampUVBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, BiosanityMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        customLamp();

        blockWithItem(ModBlocks.NEOPLASM_BLOCK);

        blockWithItem(ModBlocks.NEOPLASM_VEIN_BLOCK);

        blockWithItem(ModBlocks.NEOPLASM_DEVOUR_BLOCK);

        blockWithItem(ModBlocks.NETHER_ALGANIT_ORE);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void customLamp() {
        getVariantBuilder(ModBlocks.UV_LAMP_BLOCK.get()).forAllStates(state -> {
            if(state.getValue(RedstoneLampUVBlock.LIT)) {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("uv_lamp_block_lit",
                        new ResourceLocation(BiosanityMod.MOD_ID, "block/" + "uv_lamp_block_lit")))};
            } else {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("uv_lamp_block",
                        new ResourceLocation(BiosanityMod.MOD_ID, "block/" +"uv_lamp_block")))};
            }
        });

        simpleBlockItem(ModBlocks.UV_LAMP_BLOCK.get(), models().cubeAll("uv_lamp_block",
                new ResourceLocation(BiosanityMod.MOD_ID, "block/" +"uv_lamp_block")));
    }
}
