package net.whiteman.biosanity.world.level.neoplasm.common;

import net.whiteman.biosanity.world.level.neoplasm.hivemind.HivemindLevel;

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

    // Scan memory params
    /** Determines how old scan can be (in ticks) */
    public static final long CORE_SCAN_MAX_AGE = 200;

    //endregion

    //region Vein

    // Base params
    public static final double FALL_CHANCE = 0.75;
    public static final double ORIGINAL_DIRECTION_CHANCE = 0.45;
    public static final int REROLL_ATTEMPTS = 8;
    public static final int NEIGHBOR_LIMIT = 2;

    // Transmissing params
    public static final int TICKS_TO_TRANSFER_NUTRIENT = 5;
    public static final int TICKS_TO_SEND_IMPULSE = 3;

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
    public static final int MAX_RESOURCE_VALUE = 9999;
}