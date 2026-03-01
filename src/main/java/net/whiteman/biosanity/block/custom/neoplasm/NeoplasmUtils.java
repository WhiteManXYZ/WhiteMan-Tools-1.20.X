package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class NeoplasmUtils {
    public record ResourceEntry(NeoplasmResourceType type, int level) {}

    private static final Map<Block, ResourceEntry> DEVOUR_MAP = new HashMap<>();

    private static final Set<Block> REPLACEABLE_BLOCKS = new HashSet<>();

    private static final List<TagKey<Block>> REPLACEABLE_TAGS = new ArrayList<>();

    public static void setup() {
        DEVOUR_MAP.clear();
        // TODO(whiteman) make blocks that have same level bind to tags?
        // WIP
        register(Blocks.OAK_LOG, NeoplasmResourceType.BIOMASS, 1);
        register(Blocks.SPRUCE_LOG, NeoplasmResourceType.BIOMASS, 1);
        register(Blocks.ACACIA_LOG, NeoplasmResourceType.BIOMASS, 1);
        register(Blocks.DARK_OAK_LOG, NeoplasmResourceType.BIOMASS, 2);

        register(Blocks.COAL_ORE, NeoplasmResourceType.MINERAL, 1);
        register(Blocks.COPPER_ORE, NeoplasmResourceType.MINERAL, 1);
        register(Blocks.IRON_ORE, NeoplasmResourceType.MINERAL, 2);
        register(Blocks.GOLD_ORE, NeoplasmResourceType.MINERAL, 3);
        register(Blocks.EMERALD_ORE, NeoplasmResourceType.MINERAL, 3);
        register(Blocks.DIAMOND_ORE, NeoplasmResourceType.MINERAL, 4);

        register(Blocks.REDSTONE_ORE, NeoplasmResourceType.ENERGY, 2);
        register(Blocks.LAPIS_ORE, NeoplasmResourceType.ENERGY, 2);


        REPLACEABLE_BLOCKS.clear();
        REPLACEABLE_TAGS.clear();

        register(BlockTags.REPLACEABLE_BY_TREES);
        register(BlockTags.FLOWERS);
    }

    private static void register(Block block, NeoplasmResourceType type, int level) {
        DEVOUR_MAP.put(block, new ResourceEntry(type, level));
    }

    public static void register(Block block) {
        REPLACEABLE_BLOCKS.add(block);
    }

    public static void register(TagKey<Block> tag) {
        REPLACEABLE_TAGS.add(tag);
    }

    public static boolean isReplaceable(BlockState state) {
        if (state.isAir()) return true;

        if (REPLACEABLE_BLOCKS.contains(state.getBlock())) return true;

        for (TagKey<Block> tag : REPLACEABLE_TAGS) {
            if (state.is(tag)) return true;
        }
        return false;
    }

    public static ResourceEntry getResourceInfo(Block block) {
        return DEVOUR_MAP.getOrDefault(block, new ResourceEntry(NeoplasmResourceType.NONE, 0));
    }
}