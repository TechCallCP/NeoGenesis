package shipwrights.genesis.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.client.WormholeAmbianceHandler;

public record WormholeTravelSoundPacket(BlockPos enginePos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WormholeTravelSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "wormhole_travel_sound"));

    public static final StreamCodec<FriendlyByteBuf, WormholeTravelSoundPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    WormholeTravelSoundPacket::enginePos,
                    WormholeTravelSoundPacket::new
            );

    @Override
    public CustomPacketPayload.Type<WormholeTravelSoundPacket> type() {
        return TYPE;
    }

    public static void handle(WormholeTravelSoundPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handle(payload));
    }

    private static class ClientHandler {
        public static void handle(WormholeTravelSoundPacket packet) {
            WormholeAmbianceHandler.wormholeTravelPos = packet.enginePos();
        }
    }
}
