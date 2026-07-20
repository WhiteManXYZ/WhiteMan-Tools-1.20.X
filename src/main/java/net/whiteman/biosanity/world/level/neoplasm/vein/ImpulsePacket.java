package net.whiteman.biosanity.world.level.neoplasm.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.whiteman.biosanity.world.level.neoplasm.hivemind.HivemindLevel;

public record ImpulsePacket(
        ImpulseType type,
        HivemindLevel hiveLevel,
        BlockPos sourceCore,
        Direction sendDirection,
        int id
) {
    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", type.name());
        nbt.putString("hive_level", hiveLevel.name());
        nbt.putString("send_direction", sendDirection.name());
        nbt.putLong("source_core", sourceCore.asLong());
        nbt.putInt("id", id);
        return nbt;
    }

    public static ImpulsePacket fromNBT(CompoundTag nbt) {
        return new ImpulsePacket(
                ImpulseType.valueOf(nbt.getString("type")),
                HivemindLevel.valueOf(nbt.getString("hive_level")),
                BlockPos.of(nbt.getLong("source_core")),
                Direction.valueOf(nbt.getString("send_direction")),
                nbt.getInt("id")
        );
    }
}
