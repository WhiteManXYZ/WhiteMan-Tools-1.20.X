package net.whiteman.biosanity.world.level.neoplasm.ai.goals;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.whiteman.biosanity.world.level.block.entity.NeoplasmCoreBE;
import net.whiteman.biosanity.world.level.neoplasm.ai.AbstractGoal;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.Hivemind;
import net.whiteman.biosanity.world.level.neoplasm.vein.ImpulseType;

import java.util.List;
import java.util.Map;

import static net.whiteman.biosanity.world.level.neoplasm.common.NeoplasmConfig.CORE_SCAN_MAX_AGE;

/** Send an impulse to scan nearby blocks */
public class ScanBlocks extends AbstractGoal {
    private final int goalCooldown;
    private final int staminaCost = 5;

    private Map<Direction, Long> coreLastScans;

    private List<Direction> connectedVeins;
    private Direction nextScanCandidate = null;

    public ScanBlocks(NeoplasmCoreBE core, double baseWeight, int goalCooldown) {
        super(core, baseWeight);
        this.goalCooldown = goalCooldown;
    }

    @Override
    public boolean canUse() {
        Level level = core.getLevel();
        Hivemind hivemind = getHivemind();
        connectedVeins = core.findNeighborVeins();
        if (level == null || hivemind == null || connectedVeins == null) return false;

        // If we don't have connected veins, return
        if (connectedVeins.isEmpty()) return false;

        // If we don't have enough resources, return false
        return hivemind.getStamina() >= staminaCost;
    }

    public double evaluateUtility() {
        Hivemind hivemind = getHivemind();
        NeoplasmCoreBE coreBE = getCore();
        Level level = coreBE.getLevel();

        if (hivemind == null || level == null) return 0.0;

        long currentGameTime = level.getGameTime();
        double utility = super.evaluateUtility();

        if (utility <= 0) return 0.0;

        coreLastScans = coreBE.getLastScanTime();

        // If we don't have scans yet, increase utility
        if (coreLastScans.isEmpty()) {
            return utility * 3.0;
        }

        // Looking for oldest scan
        long oldestScanAge = 0;
        for (long scanTime : coreLastScans.values()) {
            long age = currentGameTime - scanTime;
            if (age > oldestScanAge) {
                oldestScanAge = age;
            }
        }

        // If oldest scan is expired, request new one
        if (oldestScanAge > CORE_SCAN_MAX_AGE) {
            double ageFactor = (double) oldestScanAge / CORE_SCAN_MAX_AGE;
            ageFactor = Math.min(ageFactor, 4.0);
            return utility * ageFactor;
        }

        // At this point we know everything we need, decrease utility
        return Math.max(0, utility * 0.35);
    }

    @Override public void start() {
        System.out.println(getHivemind().getId() + ": Started scan");
        resetTimer(goalCooldown);
    }

    @Override
    public void tick() {
        Level level = core.getLevel();
        Hivemind hivemind = getHivemind();
        if (level == null || hivemind == null) return;
        timer++;

        if (timer >= currentCooldown) {

            if (coreLastScans.isEmpty()) {
                nextScanCandidate = connectedVeins.get(level.random.nextInt(connectedVeins.size()));
            } else {
                Long oldestScanTime = null;
                for (Direction dir : coreLastScans.keySet()) {
                    long scanTime = coreLastScans.get(dir);

                    if (oldestScanTime == null || scanTime < oldestScanTime) {
                        oldestScanTime = scanTime;
                        nextScanCandidate = dir;
                    }
                }
            }

            if (!connectedVeins.contains(nextScanCandidate)) return;

            if (core.sendImpulse(ImpulseType.SCAN_BLOCKS, hivemind.getLevel(), nextScanCandidate)) {
                hivemind.modifyStamina(-staminaCost);
                resetTimer(goalCooldown);
            }
        }
    }

    @Override public void stop() {
        System.out.println(getHivemind().getId() + ": Stop scan");
    }
}