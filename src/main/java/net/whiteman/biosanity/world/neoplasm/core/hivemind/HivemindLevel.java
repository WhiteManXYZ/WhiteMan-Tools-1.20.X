package net.whiteman.biosanity.world.neoplasm.core.hivemind;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum HivemindLevel implements StringRepresentable {
    /// WIP
    // First stage, can only absorb resources first level
    // and dig through basic blocks (dirt, sand etc.)
    T1("tier_1", 0, 100),
    T2("tier_2", 120, 200),
    T3("tier_3", 1000, 300),
    T4("tier_4", 5000, 400),
    T5("tier_5", 30_000, 500);

    private final String name;
    private final int xp;
    private final int maxStamina;

    public static final HivemindLevel[] CORE_LEVELS = HivemindLevel.values();

    HivemindLevel(String name, int xp, int maxStamina) {
        this.name = name;
        this.xp = xp;
        this.maxStamina = maxStamina;
    }

    @Override public @NotNull String getSerializedName() {
        return this.name;
    }

    public int getNeededXp() { return xp; }

    public static int getMaxStamina(HivemindLevel level) { return level.maxStamina; }

    public static int getStartingXp(HivemindLevel level) { return level.xp; }

    public static HivemindLevel getFromXp(int xp) {
        for (int i = CORE_LEVELS.length - 1; i >= 0; i--) {
            if (xp >= CORE_LEVELS[i].xp) {
                return CORE_LEVELS[i];
            }
        }
        return HivemindLevel.T1;
    }

    public static boolean isHigherOrEqualLevel(HivemindLevel level, HivemindLevel level2) {
        return level.ordinal() >= level2.ordinal();
    }
}