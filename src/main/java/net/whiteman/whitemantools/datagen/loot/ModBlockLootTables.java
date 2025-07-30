package net.whiteman.whitemantools.datagen.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import net.whiteman.whitemantools.block.ModBlocks;
import net.whiteman.whitemantools.item.ModItems;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.UV_LAMP_BLOCK.get());
        this.dropSelf(ModBlocks.PURIFICATION_STATION_BLOCK.get());
        this.dropSelf(ModBlocks.NEOPLASM_BLOCK.get());

        this.add(ModBlocks.NETHER_ALGANIT_ORE.get(),
                block -> createCustomOreDrops(ModBlocks.NETHER_ALGANIT_ORE.get(), ModItems.ALGANIT.get(), 1, 1));
    }

    protected LootTable.Builder createCustomOreDrops(Block pBlock, Item item, int minDrop, int maxDrop) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrop, maxDrop)))
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
