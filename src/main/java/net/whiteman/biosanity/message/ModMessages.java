package net.whiteman.biosanity.message;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.whiteman.biosanity.BiosanityMod;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    // Each packet needs its own unique ID within the channel
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(BiosanityMod.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SyncNeoplasmRotPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncNeoplasmRotPacket::decode)
                .encoder(SyncNeoplasmRotPacket::encode)
                .consumerMainThread(SyncNeoplasmRotPacket::handle)
                .add();
    }

    // Method for sending a packet to everyone who "sees" a block (chunk tracking)
    public static <MSG> void sendToClientsTracking(MSG message, BlockEntity be) {
        if (be.getLevel() != null) {
            LevelChunk chunk = be.getLevel().getChunkAt(be.getBlockPos());
            INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), message);
        }
    }
}
