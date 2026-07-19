package net.whiteman.biosanity.world.neoplasm.ai;

import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBE;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;

public interface IHivemindGoal {
    NeoplasmCoreBE getCore();
    default Hivemind getHivemind() {
        return getCore().getHivemind();
    }

    /** A basic goal condition, available only if the goal is feasible
     * (e.g. sufficient resources or free space, etc.) */
    boolean canUse();

    /** Helper method (if conditions have changed, but the goal is actually still possible) */
    default boolean canContinueToUse() {
        return canUse();
    }

    /** Calculates the weight value of a goal based on current data
     * about the Hivemind, about the core that can accomplish the goal, and special factors
     * @return Calculated weight */
    double evaluateUtility();

    /** Returns the base weigh value of the goal (independent of conditions)
     * @return Base weight */
    double getBaseWeight();

    /**
     * If the goal is "unique" (e.g., building special block),
     * then we return true only if no one has taken it.
     * If the goal is "scalable" (e.g., energy production), every core can tick it. */
    default boolean isStackable() {
        return true;
    }

    /** Called when the goal starts */
    default void start() {
        // Timers etc. should start here
        // to prevent instant action
    }
    /** Calls every tick */
    void tick();
    /** Called when the goal stops */
    default void stop() {}
}