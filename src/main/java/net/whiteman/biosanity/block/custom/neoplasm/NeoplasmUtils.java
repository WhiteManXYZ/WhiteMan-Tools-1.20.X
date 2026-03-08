package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class NeoplasmUtils {
    public record ResourceEntry(NeoplasmResourceType type, int level) {}


    private static final Map<Block, ResourceEntry> DEVOUR_MAP = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, ResourceEntry> DEVOUR_MAP_TAGS = new LinkedHashMap<>();
    private static final Set<Block> REPLACEABLE_BLOCKS = new HashSet<>();
    private static final List<TagKey<Block>> REPLACEABLE_TAGS = new ArrayList<>();


    public static void setup() {
        // Important: Register individual blocks first,
        // then block tags, all in order:
        // Highest -> Lowest level
        // Biomass -> Mineral -> Energy
        /// Still in WIP

        //region Devourable registry
        DEVOUR_MAP.clear();

        register(Blocks.NETHERITE_BLOCK, NeoplasmResourceType.MINERAL, 11);

        register(Blocks.DIAMOND_BLOCK, NeoplasmResourceType.MINERAL, 9);

        register(Blocks.GOLD_BLOCK, NeoplasmResourceType.MINERAL, 8);

        register(Blocks.REDSTONE_BLOCK, NeoplasmResourceType.ENERGY, 5);

        register(Blocks.NETHER_QUARTZ_ORE, NeoplasmResourceType.MINERAL, 3);

        // -------------------------

        register(BlockTags.DIAMOND_ORES, NeoplasmResourceType.MINERAL, 4);

        register(BlockTags.CRIMSON_STEMS, NeoplasmResourceType.BIOMASS, 3);
        register(BlockTags.WARPED_STEMS, NeoplasmResourceType.BIOMASS, 3);
        register(BlockTags.GOLD_ORES, NeoplasmResourceType.MINERAL, 3);
        register(BlockTags.EMERALD_ORES, NeoplasmResourceType.MINERAL, 3);
        register(BlockTags.LAPIS_ORES, NeoplasmResourceType.ENERGY, 3);

        register(BlockTags.DARK_OAK_LOGS, NeoplasmResourceType.BIOMASS, 2);
        register(BlockTags.IRON_ORES, NeoplasmResourceType.MINERAL, 2);
        register(BlockTags.REDSTONE_ORES, NeoplasmResourceType.ENERGY, 2);

        register(BlockTags.LOGS, NeoplasmResourceType.BIOMASS, 1);
        register(BlockTags.COAL_ORES, NeoplasmResourceType.MINERAL, 1);
        register(BlockTags.COPPER_ORES, NeoplasmResourceType.MINERAL, 1);
        //endregion

        //region Replaceable registry
        REPLACEABLE_BLOCKS.clear();
        REPLACEABLE_TAGS.clear();

        register(BlockTags.REPLACEABLE_BY_TREES);
        register(BlockTags.FLOWERS);
        //endregion
    }

    private static void register(Block block, NeoplasmResourceType type, int level) {
        DEVOUR_MAP.put(block, new ResourceEntry(type, level));
    }

    private static void register(TagKey<Block> tag, NeoplasmResourceType type, int level) {
        DEVOUR_MAP_TAGS.put(tag, new ResourceEntry(type, level));
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
        if (DEVOUR_MAP.containsKey(block)) {
            return DEVOUR_MAP.get(block);
        }

        BlockState state = block.defaultBlockState();
        for (Map.Entry<TagKey<Block>, ResourceEntry> entry : DEVOUR_MAP_TAGS.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return new ResourceEntry(NeoplasmResourceType.NONE, 0);
    }
}