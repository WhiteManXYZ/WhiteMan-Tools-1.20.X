package net.whiteman.biosanity.world.level.neoplasm.ai.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.whiteman.biosanity.world.level.block.entity.NeoplasmCoreBE;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.Hivemind;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.HivemindManager;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceType;
import net.whiteman.biosanity.world.level.neoplasm.vein.ScannedResource;

import javax.annotation.Nullable;
import java.util.*;

public class NeoplasmCoreAI {
    private final NeoplasmCoreBE core;
    public Hivemind hivemind;

    private int hiveBiomass;
    private int hiveMinerals;
    private int hiveEnergy;

    /** Data from core */
    private Map<Direction, List<ScannedResource>> blockScanData = new EnumMap<>(Direction.class);
    private final Map<Direction, Long> lastScanTimeData = new EnumMap<>(Direction.class);

    public NeoplasmCoreAI(NeoplasmCoreBE core) {
        this.core = core;
        this.hivemind = core.getHivemind();
    }

    public void tick(Level level) {
        if (level.isClientSide) return;

        if (hivemind == null) {
            return;
        }

        blockScanData = core.blockScanMemory;
        updateHivemindResources();
        ResourceType lowestResource = findLowestResource();

        core.setResourcePosForDirection(findClosestResourcePos(lowestResource));
    }

    private ArrayList<PosInDirection> findClosestResourcePos(ResourceType requiredResource) {
        ArrayList<PosInDirection> resources = new ArrayList<>();

        // TODO fix: dont returns really closest resource
        // TODO check work
        for (Map.Entry<Direction, List<ScannedResource>> entry : blockScanData.entrySet()) {
            for (ScannedResource resource : entry.getValue()) {
                if (resource.types().contains(requiredResource)) {
                    resources.add(new PosInDirection(entry.getKey(), resource.pos()));
                }
            }
        }

        return resources;
    }

    private ResourceType findLowestResource() {
        int maxStorage = hivemind.getStorage();
        if (maxStorage <= 0) {
            throw new IllegalStateException("Error in neoplasmCoreAI: hivemind storage equal/lower than 0!");
        }

        ResourceType result = ResourceType.BIOMASS;
        float lowest = hiveBiomass;

        if (hiveMinerals < lowest) {
            lowest = hiveMinerals;
            result = ResourceType.MINERAL;
        }
        if (hiveEnergy < lowest) {
            result = ResourceType.ENERGY;
        }

        return result;
    }

    private void updateHivemindResources() {
        Hivemind.HivemindResources hivemindResources = hivemind.getResources();
        hiveBiomass = hivemindResources.biomass();
        hiveMinerals = hivemindResources.minerals();
        hiveEnergy = hivemindResources.energy();
    }

    public record PosInDirection(Direction direction, BlockPos pos) {}
}
