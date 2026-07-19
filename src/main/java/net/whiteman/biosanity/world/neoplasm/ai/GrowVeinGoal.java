package net.whiteman.biosanity.world.neoplasm.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBE;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.Hivemind;
import net.whiteman.biosanity.world.neoplasm.vein.ImpulseType;

import java.util.List;

/** Sends an impulse to grow from core */
public class GrowVeinGoal extends AbstractGoal {
    private final int goalCooldown;
    private final int biomassCost = 1;
    private final int staminaCost = 3;

    private List<BlockPos> connectedVeins;

    public GrowVeinGoal(NeoplasmCoreBE core, double baseWeight, int goalCooldown) {
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
        return (hivemind.getBiomass() >= biomassCost && hivemind.getStamina() >= staminaCost);
    }

    public double evaluateUtility() {
        // TODO growth weight calculate logic
        Hivemind hivemind = getHivemind();
        if (hivemind == null) return 0;

        double utility = super.evaluateUtility();
        if (utility <= 0) return 0;

        // Stamina factor (100% -> full base weight, 50% -> half etc.)
        double staminaFactor = (double) hivemind.getStamina() / hivemind.getMaxStamina();
        utility *= staminaFactor;

        // TEST
        // "Adrenaline"
        // If resources is close to be insufficient, increase "looking for resources"
        double biomassFactor = (double) hivemind.getBiomass() / hivemind.getStorage();
        if (biomassFactor < 0.15d) {
            utility += 0.3 * baseWeight;
        }

        return Math.max(0, utility);
    }

    @Override public void start() {
        System.out.println(getHivemind().getId() + ": Started growing veins!");
        resetTimer(goalCooldown);
    }

    @Override
    public void tick() {
        Level level = core.getLevel();
        Hivemind hivemind = getHivemind();
        if (level == null || hivemind == null) return;
        timer++;

        if (timer >= currentCooldown) {
            // TODO dir calculation
            BlockPos posA = core.getBlockPos();
            BlockPos posB = connectedVeins.get(level.random.nextInt(connectedVeins.size()));

            int dx = posB.getX() - posA.getX();
            int dy = posB.getY() - posA.getY();
            int dz = posB.getZ() - posA.getZ();

            Direction dir = Direction.fromDelta(dx, dy, dz);

            if (dir == null) return;

            if (core.sendImpulse(ImpulseType.GROW, hivemind.getLevel(), dir)) {
                hivemind.modifyBiomass(-biomassCost);
                hivemind.modifyStamina(-staminaCost);
                resetTimer(goalCooldown);
            }
        }
    }

    @Override public void stop() {
        System.out.println(getHivemind().getId() + ": Stop growing veins!");
    }
}