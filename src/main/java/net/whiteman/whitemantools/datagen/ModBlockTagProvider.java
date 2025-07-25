package net.whiteman.whitemantools.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.whiteman.whitemantools.WhiteManToolsMod;
import net.whiteman.whitemantools.block.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, WhiteManToolsMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.NETHER_ALGANIT_ORE.get(),
                ModBlocks.PURIFICATION_CHAMBER_BLOCK.get());

        this.tag(BlockTags.NEEDS_IRON_TOOL).add(
                ModBlocks.NETHER_ALGANIT_ORE.get(),
                ModBlocks.PURIFICATION_CHAMBER_BLOCK.get());
    }
}
