package net.whiteman.biosanity.world.neoplasm.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.HivemindLevel;

public record ImpulsePacket(
        ImpulseType type,
        HivemindLevel hiveLevel,
        BlockPos sourceCore,
        int id
) {
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", type.name());
        nbt.putString("hive_level", hiveLevel.name());
        nbt.putLong("source_core", sourceCore.asLong());
        nbt.putInt("id", id);
        return nbt;
    }

    public static ImpulsePacket fromNBT(CompoundTag nbt) {
        return new ImpulsePacket(
                ImpulseType.valueOf(nbt.getString("type")),
                HivemindLevel.valueOf(nbt.getString("hive_level")),
                BlockPos.of(nbt.getLong("source_core")),
                nbt.getInt("id")
        );
    }
}
