package net.whiteman.biosanity.world.neoplasm.core;

import net.whiteman.biosanity.world.neoplasm.core.hivemind.HivemindLevel;

import static net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry.MAX_RESOURCE_LEVEL;

public class CoreConfig {
    /// WIP
    /// Maybe make external .json config?
    public static final HivemindLevel STARTING_CORE_LEVEL = HivemindLevel.T1;
    public static final int STARTING_BIOMASS_VALUE = 30;
    public static final int STARTING_MINERALS_VALUE = 10;
    public static final int STARTING_ENERGY_VALUE = 10;
    public static final int START_MAX_STORAGE = 150;

    public static final int CORE_EXPAND_STORAGE_VALUE = 100;

    public static final int HIVEMIND_MAX_CORES = 32;
    public static final int MAX_XP = HivemindLevel.T5.getNeededXp();
    public static final int MAX_ALERT_POINTS = 10000;
    public static final int RESOURCE_LEVEL_MULTIPLIER = 10;
    public static final int[] XP_VALUES = new int[MAX_RESOURCE_LEVEL + 1];
    public static final int[] NUTRIENTS_VALUES = new int[MAX_RESOURCE_LEVEL + 1];
    public static final int CALM_DOWN_RATE = 5;
    public static final int MIN_TICKS_REACTION = 40;
    public static final int MAX_ADDITIONAL_TICKS_REACTION = 60;

    static {
        for (int i = 0; i <= MAX_RESOURCE_LEVEL; i++) {
            if (i == 0) {
                XP_VALUES[i] = 0;
                NUTRIENTS_VALUES[i] = 0;
            } else {
                double power = i - 1;
                XP_VALUES[i] = (int) (RESOURCE_LEVEL_MULTIPLIER * Math.pow(3, power));
                NUTRIENTS_VALUES[i] = (int) (RESOURCE_LEVEL_MULTIPLIER * Math.pow(2, power));
            }
        }
    }

    public static int getXPFromLevel(int level) {
        int clampedLevel = Math.max(0, Math.min(level, MAX_RESOURCE_LEVEL));
        return XP_VALUES[clampedLevel];
    }

    public static int getNutrientsFromLevel(int level) {
        int clampedLevel = Math.max(0, Math.min(level, MAX_RESOURCE_LEVEL));
        return NUTRIENTS_VALUES[clampedLevel];
    }
}