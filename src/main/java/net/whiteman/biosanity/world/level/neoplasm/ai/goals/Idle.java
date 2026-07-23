package net.whiteman.biosanity.world.level.neoplasm.ai.goals;

import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.world.level.neoplasm.ai.AbstractGoal;
import net.whiteman.biosanity.world.level.block.entity.NeoplasmCoreBE;

public class Idle extends AbstractGoal {
    private final int goalCooldown;

    public Idle(NeoplasmCoreBE core, double baseWeight, int goalCooldown) {
        super(core, baseWeight);
        this.goalCooldown = goalCooldown;
    }

    @Override public boolean canUse() {
        return true; // Always allow to rest
    }

    @Override
    public double evaluateUtility() {
        double utility = super.evaluateUtility();
        if (utility <= 0) return 0;

        double staminaPercent = (double) getHivemind().getStamina() / getHivemind().getMaxStamina();

        // If stamina lower than 20%, the desire to rest increases highly
        if (staminaPercent < 0.2d) return 70d;

        // In a normal state, the desire for rest is based on
        // how much stamina is insufficient
        return Math.max(0, utility + (1.0d - staminaPercent) * baseWeight);
    }

    @Override public void start() {
        BiosanityMod.LOGGER.debug("Idle mode.");
        resetTimer(goalCooldown);
    }

    @Override
    public void tick() {
        timer++;

        if (timer >= currentCooldown) {
            getHivemind().modifyStamina(100);
            resetTimer(goalCooldown);
        }
    }

    @Override public void stop() {
        BiosanityMod.LOGGER.debug("Idle stopped.");
    }
}