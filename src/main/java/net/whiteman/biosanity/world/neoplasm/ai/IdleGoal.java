package net.whiteman.biosanity.world.neoplasm.ai;

import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBE;

public class IdleGoal extends AbstractGoal {
    private final int goalCooldown;

    public IdleGoal(NeoplasmCoreBE core, double baseWeight, int goalCooldown) {
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
        System.out.println("Idle start");
        resetTimer(goalCooldown);
    }

    @Override
    public void tick() {
        timer++;

        if (timer >= currentCooldown) {
            getHivemind().modifyStamina(1);
            resetTimer(goalCooldown);
        }
    }

    @Override public void stop() {
        System.out.println("Idle stop");
    }
}