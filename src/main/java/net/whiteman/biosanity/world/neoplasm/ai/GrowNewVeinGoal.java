package net.whiteman.biosanity.world.neoplasm.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBE;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.AlertLevel;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;

import java.util.List;

import static net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry.isReplaceable;

public class GrowNewVeinGoal extends AbstractGoal {
    private final int goalCooldown;
    private final int biomassCost = 2;
    private final int staminaCost = 5;

    public GrowNewVeinGoal(NeoplasmCoreBE core, double baseWeight, int goalCooldown) {
        super(core, baseWeight);
        this.goalCooldown = goalCooldown;
    }

    @Override
    public boolean canUse() {
        Level level = core.getLevel();
        Hivemind hivemind = getHivemind();
        if (level == null || hivemind == null) return false;

        // If we don't space to grow, return
        if (!hasAnySpaceToGrow(level)) return false;

        // If we don't have enough resources, return false
        return (hivemind.getBiomass() >= biomassCost && hivemind.getStamina() >= staminaCost);
    }

    public double evaluateUtility() {
        Hivemind hivemind = getHivemind();
        if (hivemind == null) return 0;

        double utility = super.evaluateUtility();
        if (utility <= 0) return 0;

        // Stamina factor (100% -> full base weight, 50% -> half etc.)
        double staminaFactor = (double) hivemind.getStamina() / hivemind.getMaxStamina();
        utility *= staminaFactor;

        // If there are enough resources, we increase a little growth weight
        double biomassFactor = (double) hivemind.getBiomass() / hivemind.getStorage();
        if (biomassFactor > 0.75d) {
            utility += 0.3 * baseWeight;
        }

        // Excess factor, if core has enough veins near, decrease growth weight
        List<BlockPos> nearbyVeins = core.findNeighborVeins();
        if (nearbyVeins != null && !nearbyVeins.isEmpty()) {
            // Custom divide factor: about -1.66~ weight per nearby vein
            double nearbyAmountFactor = (double) (nearbyVeins.size() / 6) * 0.4;

            utility += -nearbyAmountFactor * baseWeight;
        }

        // Danger factor, if the Hivemind is on alert, the desire to expand highly drops
        if (hivemind.getAlertLevel() != AlertLevel.CALM) {
            utility *= 0.2d;
        }

        return Math.max(0, utility);
    }

    @Override public void start() {
        System.out.println(getHivemind().getId() + ": Started growth!");
        resetTimer(goalCooldown);
    }

    @Override
    public void tick() {
        Level level = core.getLevel();
        Hivemind hivemind = getHivemind();
        if (level == null || hivemind == null) return;
        timer++;

        if (timer >= currentCooldown) {
            Direction dir = Direction.getRandom(level.random);

            if (dir.getAxis().isVertical()) return;

            if (!isReplaceable(level.getBlockState(core.getBlockPos().relative(dir)), hivemind.getLevel())) return;

            if (core.growNewVein(dir)) {
                hivemind.modifyBiomass(-biomassCost);
                hivemind.modifyStamina(-staminaCost);
                resetTimer(goalCooldown);
            }
        }
    }

    @Override public void stop() {
        System.out.println(getHivemind().getId() + ": Stop growth.");
    }

    private boolean hasAnySpaceToGrow(Level level) {
        BlockPos pos = core.getBlockPos();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isReplaceable(level.getBlockState(pos.relative(dir)), getHivemind().getLevel())) {
                return true;
            }
        }
        return false;
    }
}