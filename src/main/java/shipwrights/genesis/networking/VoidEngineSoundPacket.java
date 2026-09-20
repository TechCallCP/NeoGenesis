package shipwrights.genesis.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.client.WormholeAmbianceHandler;

public record VoidEngineSoundPacket(BlockPos enginePos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VoidEngineSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "void_engine_sound"));

    public static final StreamCodec<FriendlyByteBuf, VoidEngineSoundPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    VoidEngineSoundPacket::enginePos,
                    VoidEngineSoundPacket::new
            );

    @Override
    public CustomPacketPayload.Type<VoidEngineSoundPacket> type() {
        return TYPE;
    }

    public static void handle(VoidEngineSoundPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handle(payload));
    }

    private static class ClientHandler {
        public static void handle(VoidEngineSoundPacket packet) {
            WormholeAmbianceHandler.voidEngineStartPos = packet.enginePos();
            WormholeAmbianceHandler.playVoidEngineStart();
        }
    }
}
