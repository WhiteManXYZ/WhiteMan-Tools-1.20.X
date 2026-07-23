package net.whiteman.biosanity.world.level.neoplasm.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.level.neoplasm.ai.AbstractGoal;
import net.whiteman.biosanity.world.level.block.entity.NeoplasmCoreBE;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.AlertLevel;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.Hivemind;

import java.util.List;

import static net.whiteman.biosanity.world.level.neoplasm.common.NeoplasmConstants.DIRECTIONS;
import static net.whiteman.biosanity.world.level.neoplasm.common.NeoplasmConfig.HIVEMIND_MAX_CORES;
import static net.whiteman.biosanity.world.level.neoplasm.resource.ResourceRegistry.isReplaceable;

public class ExpandHivemind extends AbstractGoal {
    private final int goalCooldown;
    private final int biomassCost = 15;
    private final int staminaCost = 30;

    public ExpandHivemind(NeoplasmCoreBE core, double baseWeight, int goalCooldown) {
        super(core, baseWeight);
        this.goalCooldown = goalCooldown;
    }

    @Override public boolean isStackable() { return false; }

    @Override
    public boolean canUse() {
        Level level = core.getLevel();
        Hivemind hivemind = getHivemind();
        if (level == null || hivemind == null) return false;

        // If Hivemind is full, return
        if (hivemind.getAllMembers().size() >= HIVEMIND_MAX_CORES) return false;

        // If we don't space to grow, return
        if (!hasAnySpaceToGrow(level)) return false;

        // If we don't have enough resources, return false
        return (hivemind.getBiomass() >= biomassCost && hivemind.getStamina() >= staminaCost);
    }

    @Override
    public double evaluateUtility() {
        Hivemind hivemind = getHivemind();
        if (hivemind == null) return 0;

        double utility = super.evaluateUtility();
        if (utility <= 0) return 0;

        // If there are enough resources, we increase expanding weight
        double biomassFactor = (double) hivemind.getBiomass() / hivemind.getStorage();
        if (biomassFactor > 0.85d) {
            utility += 0.7 * baseWeight;
        }

        // If storages is almost full, increase expanding weight
        double storage = hivemind.getStorage();
        if (storage > 0) {
            double fullness = hivemind.getAverageResourcesAmount() / storage;

            if (fullness > 0.8d)  utility += fullness * baseWeight;
        }

        // Clumping factor, if there too much cores near, decrease expanding weight
        List<BlockPos> neighbors = core.findNeighborCores();
        if (neighbors != null && !neighbors.isEmpty()) {
            double neighborsFactor = (double) (neighbors.size() / 6) * 0.5;

            utility += -neighborsFactor * baseWeight;
        }

        // Danger factor, if the Hivemind is on alert, the desire to expand highly drops
        if (hivemind.getAlertLevel() != AlertLevel.CALM) {
            utility *= 0.4;
        }

        return Math.max(0, utility);
    }

    @Override public void start() {
        System.out.println(getHivemind().getId() + ": core expanding...");
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

            if (!isReplaceable(level.getBlockState(core.getBlockPos().relative(dir)), hivemind.getLevel())
                    && !level.getBlockState(core.getBlockPos().relative(dir)).is(ModBlocks.NEOPLASM_VEIN_BLOCK.get())) return;

            if (core.expandCore(dir)) {
                hivemind.modifyBiomass(-biomassCost);
                hivemind.modifyStamina(-staminaCost);
                resetTimer(goalCooldown);
            }
        }
    }

    @Override public void stop() {
        System.out.println(getHivemind().getId() + ": Stopped expand...");
    }

    private boolean hasAnySpaceToGrow(Level level) {
        BlockPos pos = core.getBlockPos();
        for (Direction dir : DIRECTIONS) {
            if (isReplaceable(level.getBlockState(pos.relative(dir)), getHivemind().getLevel()) || level.getBlockState(pos.relative(dir)).is(ModBlocks.NEOPLASM_VEIN_BLOCK.get())) {
                return true;
            }
        }
        return false;
    }
}