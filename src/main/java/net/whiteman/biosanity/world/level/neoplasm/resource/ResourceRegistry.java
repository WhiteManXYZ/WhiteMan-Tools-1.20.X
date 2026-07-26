package net.whiteman.biosanity.world.level.neoplasm.resource;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.HivemindLevel;

import javax.annotation.Nullable;
import java.util.*;

import static net.whiteman.biosanity.world.level.neoplasm.hivemind.HivemindLevel.isHigherOrEqualLevel;

public class ResourceRegistry {
    public record ResourceTypeEntry(Map<ResourceType, Integer> resource) {}

    private static final Map<Block, ResourceTypeEntry> DEVOUR_MAP = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, ResourceTypeEntry> DEVOUR_MAP_TAGS = new LinkedHashMap<>();
    private static final Map<Block, HivemindLevel> REPLACEABLE_BLOCKS = new HashMap<>();
    private static final Map<TagKey<Block>, HivemindLevel> REPLACEABLE_TAGS = new HashMap<>();

    public static void setup() {
        // TODO rewrite to new system
        // Important: Register individual blocks first,
        // then block tags, all in order:
        // Highest -> Lowest amount
        /// Still in WIP

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

    public static @Nullable ResourceTypeEntry getResourceInfo(Block block) {
        if (DEVOUR_MAP.containsKey(block)) {
            return DEVOUR_MAP.get(block);
        }

        BlockState state = block.defaultBlockState();
        for (Map.Entry<TagKey<Block>, ResourceTypeEntry> entry : DEVOUR_MAP_TAGS.entrySet()) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static void register(Block block, Map<ResourceType, Integer> resourceMap) {
        DEVOUR_MAP.put(block, new ResourceTypeEntry(resourceMap));
    }

    private static void register(TagKey<Block> tag, Map<ResourceType, Integer> resourceMap) {
        DEVOUR_MAP_TAGS.put(tag, new ResourceTypeEntry(resourceMap));
    }

    public static void register(Block block, HivemindLevel level) {
        REPLACEABLE_BLOCKS.put(block, level);
    }

    public static void register(TagKey<Block> tag, HivemindLevel level) {
        REPLACEABLE_TAGS.put(tag, level);
    }
}