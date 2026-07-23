package net.whiteman.biosanity.world.level.neoplasm.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceType;

public record ScannedResource(
        BlockPos pos,
        Block block,
        int distance,
        long scanTime,
        ResourceType type
) {
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        String blockName = "minecraft:air";

        if (block != null) {
            ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
            if (rl != null) {
                blockName = rl.toString();
            }
        }

        nbt.putLong("pos", pos.asLong());
        nbt.putString("block", blockName);
        nbt.putInt("distance", distance);
        nbt.putLong("scan_time", scanTime);
        nbt.putString("type", type.name());
        return nbt;
    }

    public static ScannedResource fromNBT(CompoundTag nbt) {
        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(nbt.getString("block")));

        return new ScannedResource(
                BlockPos.of(nbt.getLong("pos")),
                block,
                nbt.getInt("distance"),
                nbt.getLong("scan_type"),
                ResourceType.valueOf(nbt.getString("type"))
        );
    }
}
