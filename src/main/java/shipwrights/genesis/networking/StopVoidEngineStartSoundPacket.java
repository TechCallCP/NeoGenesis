package shipwrights.genesis.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.client.WormholeAmbianceHandler;

public record StopVoidEngineStartSoundPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StopVoidEngineStartSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "stop_void_engine_start_sound"));

    public static final StreamCodec<FriendlyByteBuf, StopVoidEngineStartSoundPacket> STREAM_CODEC =
            StreamCodec.unit(new StopVoidEngineStartSoundPacket());

    @Override
    public CustomPacketPayload.Type<StopVoidEngineStartSoundPacket> type() {
        return TYPE;
    }

    public static void handle(StopVoidEngineStartSoundPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientHandler.handle(payload));
    }

    private static class ClientHandler {
        public static void handle(StopVoidEngineStartSoundPacket packet) {
            WormholeAmbianceHandler.stopVoidEngineStart();
        }
    }
}
