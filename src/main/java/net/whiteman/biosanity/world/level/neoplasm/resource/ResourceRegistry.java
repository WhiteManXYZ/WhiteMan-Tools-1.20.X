package net.whiteman.biosanity.world.level.neoplasm.resource;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.HivemindLevel;

import java.util.*;

import static net.whiteman.biosanity.world.level.neoplasm.hivemind.HivemindLevel.isHigherOrEqualLevel;

public class ResourceRegistry {
    public static final int MAX_RESOURCE_LEVEL = 7;
    public record ResourceTypeEntry(ResourceType resourceType, int level) {}

    private static final Map<Block, ResourceTypeEntry> DEVOUR_MAP = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, ResourceTypeEntry> DEVOUR_MAP_TAGS = new LinkedHashMap<>();
    private static final Map<Block, HivemindLevel> REPLACEABLE_BLOCKS = new HashMap<>();
    private static final Map<TagKey<Block>, HivemindLevel> REPLACEABLE_TAGS = new HashMap<>();

    public static void setup() {
        // Important: Register individual blocks first,
        // then block tags, all in order:
        // Highest -> Lowest level
        /// Still in WIP

        //region Devourable registry
        DEVOUR_MAP.clear();

        // LEVEL 7: THE PEAK
        register(Blocks.NETHERITE_BLOCK, ResourceType.MINERAL, 7);

        // LEVEL 6: PRECIOUS BLOCKS
        register(Blocks.DIAMOND_BLOCK, ResourceType.MINERAL, 6);
        register(Blocks.EMERALD_BLOCK, ResourceType.MINERAL, 6);
        register(Blocks.GOLD_BLOCK, ResourceType.MINERAL, 6);

        // LEVEL 5: CONCENTRATED ENERGY / BLOCKS
        register(Blocks.REDSTONE_BLOCK, ResourceType.ENERGY, 5);
        register(Blocks.IRON_BLOCK, ResourceType.MINERAL, 5);
        register(Blocks.LAPIS_BLOCK, ResourceType.ENERGY, 5);

        // LEVEL 4: RARE ORES
        register(Blocks.ANCIENT_DEBRIS, ResourceType.MINERAL, 4);
        register(BlockTags.DIAMOND_ORES, ResourceType.MINERAL, 4);

        // LEVEL 3: EXOTIC & NETHER
        register(Blocks.NETHER_QUARTZ_ORE, ResourceType.MINERAL, 3);
        register(Blocks.SHROOMLIGHT, ResourceType.MINERAL, 3);
        register(BlockTags.GOLD_ORES, ResourceType.MINERAL, 3);
        register(BlockTags.EMERALD_ORES, ResourceType.MINERAL, 3);
        register(BlockTags.CRIMSON_STEMS, ResourceType.BIOMASS, 3);
        register(BlockTags.WARPED_STEMS, ResourceType.BIOMASS, 3);

        // LEVEL 2: UTILITY MINERALS
        register(Blocks.BONE_BLOCK, ResourceType.BIOMASS, 2);
        register(BlockTags.IRON_ORES, ResourceType.MINERAL, 2);
        register(BlockTags.LAPIS_ORES, ResourceType.ENERGY, 2);
        register(BlockTags.REDSTONE_ORES, ResourceType.ENERGY, 2);

        // LEVEL 1: BASE BIOMASS & FUEL
        register(Blocks.HAY_BLOCK, ResourceType.BIOMASS, 1);
        register(Blocks.PUMPKIN, ResourceType.BIOMASS, 1);
        register(Blocks.CARVED_PUMPKIN, ResourceType.BIOMASS, 1);
        register(Blocks.JACK_O_LANTERN, ResourceType.BIOMASS, 1);
        register(Blocks.MELON, ResourceType.BIOMASS, 1);
        register(Blocks.RED_MUSHROOM_BLOCK, ResourceType.BIOMASS, 1);
        register(Blocks.BROWN_MUSHROOM_BLOCK, ResourceType.BIOMASS, 1);
        register(Blocks.MUSHROOM_STEM, ResourceType.BIOMASS, 1);
        register(BlockTags.LOGS, ResourceType.BIOMASS, 1);
        register(BlockTags.COAL_ORES, ResourceType.MINERAL, 1);
        register(BlockTags.COPPER_ORES, ResourceType.MINERAL, 1);
        //endregion

        //region Replaceable registry
        REPLACEABLE_BLOCKS.clear();
        REPLACEABLE_TAGS.clear();

        register(BlockTags.REPLACEABLE_BY_TREES, HivemindLevel.T1);
        register(BlockTags.FLOWERS, HivemindLevel.T1);
        register(BlockTags.DIRT, HivemindLevel.T1);
        register(BlockTags.SAND, HivemindLevel.T1);
        register(BlockTags.SNOW, HivemindLevel.T1);
        register(BlockTags.CORAL_BLOCKS, HivemindLevel.T1);
        register(BlockTags.WOODEN_STAIRS, HivemindLevel.T1);
        register(BlockTags.PLANKS, HivemindLevel.T1);
        register(Blocks.GRAVEL, HivemindLevel.T1);
        register(Blocks.CLAY, HivemindLevel.T1);
        register(Blocks.MOSS_CARPET, HivemindLevel.T1);
        register(Blocks.FARMLAND, HivemindLevel.T1);
        //endregion
    }

    public static boolean isReplaceable(BlockState state, HivemindLevel currentLevel) {
        if (state.isAir() || state.canBeReplaced()) return true;

        HivemindLevel requiredForBlock = REPLACEABLE_BLOCKS.get(state.getBlock());
        if (requiredForBlock != null && isHigherOrEqualLevel(currentLevel, requiredForBlock)) return true;

        for (TagKey<Block> tag : REPLACEABLE_TAGS.keySet()) {
            if (state.is(tag) && isHigherOrEqualLevel(currentLevel, REPLACEABLE_TAGS.get(tag))) return true;
        }

        return false;
    }

    public static boolean isResource(Block block) {
        if (DEVOUR_MAP.containsKey(block)) return true;

        BlockState state = block.defaultBlockState();
        for (Map.Entry<TagKey<Block>, ResourceTypeEntry> entry : DEVOUR_MAP_TAGS.entrySet()) {
            if (state.is(entry.getKey())) {
                return true;
            }
        }

        return false;
    }

    public static ResourceTypeEntry getResourceInfo(Block block) {
        if (DEVOUR_MAP.containsKey(block)) {
            return DEVOUR_MAP.get(block);
        }

        BlockState state = block.defaultBlockState();
        for (Map.Entry<TagKey<Block>, ResourceTypeEntry> entry : DEVOUR_MAP_TAGS.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return new ResourceTypeEntry(ResourceType.NONE, 0);
    }

    private static void register(Block block, ResourceType resourceType, int level) {
        if (level > MAX_RESOURCE_LEVEL) {
            throw new IllegalArgumentException("Error in ResourceRegistry: Level " + level +
                    " is higher than max allowed: " + MAX_RESOURCE_LEVEL + ". Use lower value instead.");
        }

        DEVOUR_MAP.put(block, new ResourceTypeEntry(resourceType, level));
    }

    private static void register(TagKey<Block> tag, ResourceType resourceType, int level) {
        if (level > MAX_RESOURCE_LEVEL) {
            throw new IllegalArgumentException("Error in ResourceRegistry: Level " + level +
                    " is higher than max allowed: " + MAX_RESOURCE_LEVEL + ". Use lower value instead.");
        }

        DEVOUR_MAP_TAGS.put(tag, new ResourceTypeEntry(resourceType, level));
    }

    public static void register(Block block, HivemindLevel level) {
        REPLACEABLE_BLOCKS.put(block, level);
    }

    public static void register(TagKey<Block> tag, HivemindLevel level) {
        REPLACEABLE_TAGS.put(tag, level);
    }
}