package net.whiteman.biosanity.message;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.whiteman.biosanity.block.entity.custom.NeoplasmRotBlockEntity;

import java.util.function.Supplier;

public class SyncNeoplasmRotPacket {
    private final BlockPos pos;
    private final CompoundTag nbt;

    public SyncNeoplasmRotPacket(BlockPos pos, CompoundTag nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    public static void encode(SyncNeoplasmRotPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeNbt(msg.nbt);
    }

    public static SyncNeoplasmRotPacket decode(FriendlyByteBuf buffer) {
        return new SyncNeoplasmRotPacket(buffer.readBlockPos(), buffer.readNbt());
    }

    public static void handle(SyncNeoplasmRotPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null && level.getBlockEntity(msg.pos) instanceof NeoplasmRotBlockEntity be) {
                be.load(msg.nbt);
                be.requestModelDataUpdate();
                level.sendBlockUpdated(msg.pos, be.getBlockState(), be.getBlockState(), 3);
            }
        });
        context.setPacketHandled(true);
    }
}
