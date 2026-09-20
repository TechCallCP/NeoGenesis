package shipwrights.genesis.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.client.ClientStorage;

public record EnteringWarpPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EnteringWarpPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "entering_warp"));

    public static final StreamCodec<FriendlyByteBuf, EnteringWarpPacket> STREAM_CODEC =
            StreamCodec.unit(new EnteringWarpPacket());

    @Override
    public CustomPacketPayload.Type<EnteringWarpPacket> type() {
        return TYPE;
    }

    public static void handle(EnteringWarpPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientStorage.goingToFromWormhole = true;
        });
    }
}
