package net.whiteman.biosanity.world.level.neoplasm.ai;

import net.minecraft.util.RandomSource;
import net.whiteman.biosanity.world.level.block.entity.NeoplasmCoreBE;

import static net.whiteman.biosanity.world.level.neoplasm.common.NeoplasmConfig.*;

public abstract class AbstractGoal implements IHivemindGoal {
    protected final NeoplasmCoreBE core;
    protected final double baseWeight;
    protected int timer;
    protected int currentCooldown;

    public AbstractGoal(NeoplasmCoreBE core, double baseWeight) {
        this.core = core;
        this.baseWeight = baseWeight;
    }

    @Override public NeoplasmCoreBE getCore() { return core; }

    @Override public double getBaseWeight() { return baseWeight; }

    @Override
    public double evaluateUtility() {
        // If global condition false, weight always 0
        if (!canUse()) return 0;

        double utility = getBaseWeight();

        // Core scatter
        utility += core.getGoalConditionOffset();

        // Goal "inertion"
        if (core.getCurrentGoal() != null && core.getCurrentGoal().getClass() == this.getClass()) {
            utility += GOAL_INERTION;
        }

        return Math.max(0, utility);
    }

    /** Resets goal cooldown, bases on:
     * the base cooldown,
     * specific core scatter and little jitter
     * @param baseDelay Base goal cooldown */
    protected void resetTimer(int baseDelay) {
        if (core.getLevel() == null) return;
        RandomSource random = core.getLevel().random;

        // Core scatter
        int offset = core.getGoalTickOffset();

        // Small random scatter
        int jitter = random.nextInt(JITTER_THRESHOLD + 1) - JITTER_OFFSET;

        this.currentCooldown = Math.max(MIN_GOAL_COOLDOWN, baseDelay + offset + jitter);
        this.timer = 0;
    }
}