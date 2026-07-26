package net.whiteman.biosanity.world.level.neoplasm.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceType;

import java.util.HashSet;
import java.util.Set;

public record ScannedResource(
        BlockPos pos,
        Block block,
        int distance,
        long scanTime,
        Set<ResourceType> types
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

        ListTag typesList = new ListTag();
        for (ResourceType item : types) {
            CompoundTag tag = new CompoundTag();

            tag.putString("name", item.name());

            typesList.add(tag);
        }
        nbt.put("types", typesList);

        return nbt;
    }

    public static ScannedResource fromNBT(CompoundTag nbt) {
        Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(nbt.getString("block")));
        HashSet<ResourceType> resourceTypes = new HashSet<>();

        for (Tag tag : nbt.getList("types", Tag.TAG_COMPOUND)) {
            if (tag instanceof CompoundTag pTag) {
                Tag item = pTag.get("name");
                if (item != null) {
                    String name = item.toString();

                    resourceTypes.add(ResourceType.valueOf(name));
                }
            }
        }

        return new ScannedResource(
                BlockPos.of(nbt.getLong("pos")),
                block,
                nbt.getInt("distance"),
                nbt.getLong("scan_type"),
                resourceTypes
        );
    }
}
