package net.whiteman.biosanity.world.level.neoplasm.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.world.level.neoplasm.ai.AbstractGoal;
import net.whiteman.biosanity.world.level.block.entity.NeoplasmCoreBE;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.AlertLevel;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.Hivemind;

import java.util.List;

import static net.whiteman.biosanity.world.level.neoplasm.resource.ResourceRegistry.isReplaceable;

public class GrowNewVein extends AbstractGoal {
    private final int goalCooldown;
    private final int biomassCost = 2;
    private final int staminaCost = 5;

    public GrowNewVein(NeoplasmCoreBE core, double baseWeight, int goalCooldown) {
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

        // If there are enough resources, we increase a little growth weight
        double biomassFactor = (double) hivemind.getBiomass() / hivemind.getStorage();
        if (biomassFactor > 0.75d) {
            utility += 0.3 * baseWeight;
        }

        // Excess factor, if core has enough veins near, decrease growth weight
        List<Direction> nearbyVeins = core.findNeighborVeins();
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
        BiosanityMod.LOGGER.debug("Started new vein growth.");
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
        BiosanityMod.LOGGER.debug("Stopped new vein growth.");
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