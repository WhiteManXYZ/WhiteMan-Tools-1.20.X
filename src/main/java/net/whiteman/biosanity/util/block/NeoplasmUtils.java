package net.whiteman.biosanity.util.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NeoplasmUtils {
    public static final int MAX_RESOURCE_LEVEL = 7;

    public static class CoreRegistry {
        public static final int MAX_XP = 1000;
        public static final int MAX_ALERT_POINTS = 10000;
        public static final int RESOURCE_LEVEL_MULTIPLIER = 10;
        private static final int[] XP_VALUES = new int[MAX_RESOURCE_LEVEL + 1];

        static {
            for (int i = 0; i <= MAX_RESOURCE_LEVEL; i++) {
                double power = Math.max(0, i - 1);
                XP_VALUES[i] = (int) (RESOURCE_LEVEL_MULTIPLIER * Math.pow(3, power));
            }
        }

        public enum CoreLevel implements StringRepresentable {
            T1("tier_1", 0),
            T2("tier_2", 120),
            T3("tier_3", 1000),
            T4("tier_4", 5000),
            T5("tier_5", 30_000);

            private final String name;
            private final int xp;

            CoreLevel(String name, int xp) {
                this.name = name;
                this.xp = xp;
            }

            @Override
            public @NotNull String getSerializedName() {
                return this.name;
            }

            public int getNeededXp() { return xp; }

            public static int getStartingXp() { return T1.xp; }
        }

        public enum CoreAlertLevel implements StringRepresentable {
            CALM("calm", 0),
            WATCHING("watching", 500),
            STRESSED("stressed", 2000),
            CRITICAL("critical", 5000);

            private final String name;
            private final int alertPoints;

            CoreAlertLevel(String name, int alertPoints) {
                this.name = name;
                this.alertPoints = alertPoints;
            }

            @Override
            public @NotNull String getSerializedName() {
                return this.name;
            }

            public static CoreAlertLevel fromPoints(int points) {
                CoreAlertLevel[] levels = CoreAlertLevel.values();
                for (int i = levels.length - 1; i >= 0; i--) {
                    if (points >= levels[i].alertPoints) {
                        return levels[i];
                    }
                }
                return CALM;
            }
        }

        public static int getXPFromLevel(int level) {
            if (level <= 0) return 0;
            if (level > MAX_RESOURCE_LEVEL) return XP_VALUES[MAX_RESOURCE_LEVEL];
            return XP_VALUES[level];
        }
    }

    public static class ResourceRegistry {
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
            if (level > MAX_RESOURCE_LEVEL) {
                throw new IllegalArgumentException("Error in NeoplasmUtils: Level " + level +
                        " is higher than max allowed: " + MAX_RESOURCE_LEVEL + ". Use lower value instead.");
            }

            DEVOUR_MAP.put(block, new ResourceTypeEntry(resourceType, level));
        }

        private static void register(TagKey<Block> tag, ResourceType resourceType, int level) {
            if (level > MAX_RESOURCE_LEVEL) {
                throw new IllegalArgumentException("Error in NeoplasmUtils: Level " + level +
                        " is higher than max allowed: " + MAX_RESOURCE_LEVEL + ". Use lower value instead.");
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
}