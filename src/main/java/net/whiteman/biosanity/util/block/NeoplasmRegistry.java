package net.whiteman.biosanity.util.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.block.custom.neoplasm.NeoplasmRotBlock;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NeoplasmRegistry {
    public record ResourceTypeEntry(ResourceType resourceType, int level) {}

    private static final Map<Block, ResourceTypeEntry> DEVOUR_MAP = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, ResourceTypeEntry> DEVOUR_MAP_TAGS = new LinkedHashMap<>();
    private static final Set<Block> REPLACEABLE_BLOCKS = new HashSet<>();
    private static final List<TagKey<Block>> REPLACEABLE_TAGS = new ArrayList<>();

    public enum ResourceType implements StringRepresentable {
        NONE("none"),
        BIOMASS("biomass"),
        MINERAL("mineral"),
        ENERGY("energy");

        private final String name;

        ResourceType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        public boolean isResource() {
            return this != NONE;
        }
    }

    public static void setup() {
        // Important: Register individual blocks first,
        // then block tags, all in order:
        // Highest -> Lowest level
        // Biomass -> Mineral -> Energy
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
        register(BlockTags.GOLD_ORES, ResourceType.MINERAL, 3);
        register(BlockTags.EMERALD_ORES, ResourceType.MINERAL, 3);
        register(BlockTags.CRIMSON_STEMS, ResourceType.BIOMASS, 3);
        register(BlockTags.WARPED_STEMS, ResourceType.BIOMASS, 3);

        // LEVEL 2: UTILITY MINERALS
        register(BlockTags.IRON_ORES, ResourceType.MINERAL, 2);
        register(BlockTags.LAPIS_ORES, ResourceType.ENERGY, 2);
        register(BlockTags.REDSTONE_ORES, ResourceType.ENERGY, 2);

        // LEVEL 1: BASE BIOMASS & FUEL
        register(BlockTags.LOGS, ResourceType.BIOMASS, 1);
        register(BlockTags.COAL_ORES, ResourceType.MINERAL, 1);
        register(BlockTags.COPPER_ORES, ResourceType.MINERAL, 1);
        //endregion

        //region Replaceable registry
        REPLACEABLE_BLOCKS.clear();
        REPLACEABLE_TAGS.clear();

        register(BlockTags.REPLACEABLE_BY_TREES);
        register(BlockTags.FLOWERS);
        register(BlockTags.DIRT);
        register(BlockTags.SAND);
        register(BlockTags.SNOW);
        register(BlockTags.CORAL_BLOCKS);
        register(BlockTags.WOODEN_STAIRS);
        register(BlockTags.PLANKS);
        register(Blocks.GRAVEL);
        register(Blocks.CLAY);
        register(Blocks.MOSS_CARPET);
        register(Blocks.FARMLAND);
        //endregion
    }

    public static boolean isReplaceable(BlockState state) {
        if (state.isAir()) return true;

        if (REPLACEABLE_BLOCKS.contains(state.getBlock())) return true;

        for (TagKey<Block> tag : REPLACEABLE_TAGS) {
            if (state.is(tag)) return true;
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
        if (level > NeoplasmRotBlock.MAX_RESOURCE_LEVEL) {
            throw new IllegalArgumentException("Error in NeoplasmRegistry: Level " + level +
                    " is higher than max allowed: " + NeoplasmRotBlock.MAX_RESOURCE_LEVEL + ". Use lower value instead.");
        }

        DEVOUR_MAP.put(block, new ResourceTypeEntry(resourceType, level));
    }

    private static void register(TagKey<Block> tag, ResourceType resourceType, int level) {
        if (level > NeoplasmRotBlock.MAX_RESOURCE_LEVEL) {
            throw new IllegalArgumentException("Error in NeoplasmRegistry: Level " + level +
                    " is higher than max allowed: " + NeoplasmRotBlock.MAX_RESOURCE_LEVEL + ". Use lower value instead.");
        }

        DEVOUR_MAP_TAGS.put(tag, new ResourceTypeEntry(resourceType, level));
    }

    public static void register(Block block) {
        REPLACEABLE_BLOCKS.add(block);
    }

    public static void register(TagKey<Block> tag) {
        REPLACEABLE_TAGS.add(tag);
    }
}