package shipwrights.genesis.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import shipwrights.genesis.NeoGenesisMod;

public record SyncTimeOffsetPacket(long timeOffset) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncTimeOffsetPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "sync_time_offset"));

    public static final StreamCodec<FriendlyByteBuf, SyncTimeOffsetPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    SyncTimeOffsetPacket::timeOffset,
                    SyncTimeOffsetPacket::new
            );

    @Override
    public CustomPacketPayload.Type<SyncTimeOffsetPacket> type() {
        return TYPE;
    }

    public static void handle(SyncTimeOffsetPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            NeoGenesisMod.clientTimeOffset = payload.timeOffset();
        });
    }
}
