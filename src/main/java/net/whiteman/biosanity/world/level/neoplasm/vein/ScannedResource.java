package net.whiteman.biosanity.world.level.neoplasm.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceType;

public record ScannedResource(BlockPos pos, Block block, int distance, long scanTime, ResourceType type) {

    public boolean isExpired(long currentGameTime, long maxAgeTicks) {
        return currentGameTime - scanTime > maxAgeTicks;
    }
}
