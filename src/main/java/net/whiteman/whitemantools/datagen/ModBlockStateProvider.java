package net.whiteman.whitemantools.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.whitemantools.WhiteManToolsMod;
import net.whiteman.whitemantools.block.ModBlocks;
import net.whiteman.whitemantools.block.custom.RedstoneLampUVBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, WhiteManToolsMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        customLamp();

        blockWithItem(ModBlocks.NEOPLASM_BLOCK);

        blockWithItem(ModBlocks.NETHER_ALGANIT_ORE);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void customLamp() {
        getVariantBuilder(ModBlocks.UV_LAMP_BLOCK.get()).forAllStates(state -> {
            if(state.getValue(RedstoneLampUVBlock.LIT)) {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("uv_lamp_block_lit",
                        new ResourceLocation(WhiteManToolsMod.MOD_ID, "block/" + "uv_lamp_block_lit")))};
            } else {
                return new ConfiguredModel[]{new ConfiguredModel(models().cubeAll("uv_lamp_block",
                        new ResourceLocation(WhiteManToolsMod.MOD_ID, "block/" +"uv_lamp_block")))};
            }
        });

        simpleBlockItem(ModBlocks.UV_LAMP_BLOCK.get(), models().cubeAll("uv_lamp_block",
                new ResourceLocation(WhiteManToolsMod.MOD_ID, "block/" +"uv_lamp_block")));
    }
}
