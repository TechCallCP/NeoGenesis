package shipwrights.genesis.networking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import shipwrights.genesis.NeoGenesisMod;

public class GenesisNetworking {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(GenesisNetworking::register);
    }

    private static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(NeoGenesisMod.MOD_ID)
                .versioned("1");

        registrar.playToClient(
                EnteringWarpPacket.TYPE,
                EnteringWarpPacket.STREAM_CODEC,
                EnteringWarpPacket::handle
        );

        registrar.playToClient(
                StopVoidEngineStartSoundPacket.TYPE,
                StopVoidEngineStartSoundPacket.STREAM_CODEC,
                StopVoidEngineStartSoundPacket::handle
        );
    }

    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}
