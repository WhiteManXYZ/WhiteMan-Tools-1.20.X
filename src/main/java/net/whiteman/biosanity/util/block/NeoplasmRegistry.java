package net.whiteman.biosanity.util.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NeoplasmRegistry {
    public record ResourceEntry(Type type, int level) {}

    private static final Map<Block, ResourceEntry> DEVOUR_MAP = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, ResourceEntry> DEVOUR_MAP_TAGS = new LinkedHashMap<>();
    private static final Set<Block> REPLACEABLE_BLOCKS = new HashSet<>();
    private static final List<TagKey<Block>> REPLACEABLE_TAGS = new ArrayList<>();

    public enum Type implements StringRepresentable {
        NONE("none"),
        BIOMASS("biomass"),
        MINERAL("mineral"),
        ENERGY("energy");

        private final String name;

        Type(String name) {
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

        register(Blocks.NETHERITE_BLOCK, Type.MINERAL, 11);

        register(Blocks.DIAMOND_BLOCK, Type.MINERAL, 9);

        register(Blocks.GOLD_BLOCK, Type.MINERAL, 8);

        register(Blocks.REDSTONE_BLOCK, Type.ENERGY, 5);

        register(Blocks.NETHER_QUARTZ_ORE, Type.MINERAL, 3);

        // -------------------------

        register(BlockTags.DIAMOND_ORES, Type.MINERAL, 4);

        register(BlockTags.CRIMSON_STEMS, Type.BIOMASS, 3);
        register(BlockTags.WARPED_STEMS, Type.BIOMASS, 3);
        register(BlockTags.GOLD_ORES, Type.MINERAL, 3);
        register(BlockTags.EMERALD_ORES, Type.MINERAL, 3);
        register(BlockTags.LAPIS_ORES, Type.ENERGY, 3);

        register(BlockTags.DARK_OAK_LOGS, Type.BIOMASS, 2);
        register(BlockTags.IRON_ORES, Type.MINERAL, 2);
        register(BlockTags.REDSTONE_ORES, Type.ENERGY, 2);

        register(BlockTags.LOGS, Type.BIOMASS, 1);
        register(BlockTags.COAL_ORES, Type.MINERAL, 1);
        register(BlockTags.COPPER_ORES, Type.MINERAL, 1);
        //endregion

        //region Replaceable registry
        REPLACEABLE_BLOCKS.clear();
        REPLACEABLE_TAGS.clear();

        register(BlockTags.REPLACEABLE_BY_TREES);
        register(BlockTags.FLOWERS);
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

        return new ResourceEntry(Type.NONE, 0);
    }

    private static void register(Block block, Type type, int level) {
        DEVOUR_MAP.put(block, new ResourceEntry(type, level));
    }

    private static void register(TagKey<Block> tag, Type type, int level) {
        DEVOUR_MAP_TAGS.put(tag, new ResourceEntry(type, level));
    }

    public static void register(Block block) {
        REPLACEABLE_BLOCKS.add(block);
    }

    public static void register(TagKey<Block> tag) {
        REPLACEABLE_TAGS.add(tag);
    }
}