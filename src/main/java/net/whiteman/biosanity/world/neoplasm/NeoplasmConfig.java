package net.whiteman.biosanity.world.neoplasm;

import net.whiteman.biosanity.world.neoplasm.core.hivemind.HivemindLevel;

import static net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry.MAX_RESOURCE_LEVEL;

public class NeoplasmConfig {
    /// WIP
    /// Maybe make external .json config?

    //region Hivemind

    // Starting resources params
    public static final HivemindLevel STARTING_HIVEMIND_LEVEL = HivemindLevel.T1;
    public static final int STARTING_BIOMASS_VALUE = 30;
    public static final int STARTING_MINERALS_VALUE = 10;
    public static final int STARTING_ENERGY_VALUE = 10;
    public static final int START_MAX_STORAGE = 150;

    // Base params
    public static final int HIVEMIND_MAX_CORES = 32;
    public static final HivemindLevel MAX_HIVEMIND_LEVEL= HivemindLevel.T5;
    public static final int MAX_XP = MAX_HIVEMIND_LEVEL.getNeededXp();

    public static final int MAX_ALERT_POINTS = 10000;
    public static final int CALM_DOWN_RATE = 5;

    public static final int TICKS_REACTION_THRESHOLD = 80;
    public static final int MIN_TICKS_REACTION = 30;

    //endregion

    //region Core

    // Base params
    public static final int CORE_EXPAND_STORAGE_VALUE = 100;
    /** Determines how much each core loads hivemind (in ticks) */
    public static final int CORE_REACTION_LOAD_VALUE = 5;

    // Goal randomization parameters
    public static final int CORE_GOAL_TICK_THRESHOLD = 30;
    public static final int CORE_GOAL_TICK_OFFSET = CORE_GOAL_TICK_THRESHOLD / 2;

    public static final int CORE_GOAL_CONDITION_THRESHOLD = 10;
    public static final int CORE_GOAL_CONDITION_OFFSET = CORE_GOAL_CONDITION_THRESHOLD / 2;

    //endregion

    //region Goal

    public static final int MIN_GOAL_COOLDOWN = 5;

    /** If the core is already working on task,
     * add weight a little so it doesn't "jerk" on other tasks with similar weight */
    public static final double GOAL_INERTION = 4.0d;

    public static final int JITTER_THRESHOLD = 8;
    public static final int JITTER_OFFSET = JITTER_THRESHOLD / 2;

    //endregion

    // Resources params
    /** Determines how much experience points gain Hivemind from level (type doesn't matter) */
    public static final int[] XP_VALUES = new int[]{0, 10, 30, 90, 270, 810, 2430, 7290};
    /** Determines how much resources gain Hivemind from level (type doesn't matter) */
    public static final int[] NUTRIENTS_VALUES = new int[]{0, 10, 20, 40, 80, 160, 320, 640};

    public static int getXPFromLevel(int level) {
        int clampedLevel = Math.max(0, Math.min(level, MAX_RESOURCE_LEVEL));
        return XP_VALUES[clampedLevel];
    }

    public static int getNutrientsFromLevel(int level) {
        int clampedLevel = Math.max(0, Math.min(level, MAX_RESOURCE_LEVEL));
        return NUTRIENTS_VALUES[clampedLevel];
    }
}