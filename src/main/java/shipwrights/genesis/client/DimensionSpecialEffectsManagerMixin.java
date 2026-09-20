package shipwrights.genesis.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

import shipwrights.genesis.NeoGenesisMod;

import static shipwrights.genesis.NeoGenesisMod.*;

@EventBusSubscriber(modid = NeoGenesisMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class DimensionEffectsRegistration_2 {

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(SPACE_DIM, new SpaceDimensionEffects());
        event.register(WORMHOLE_DIM, new WormholeDimensionEffects());
        event.register(GENERIC_PLANET_ID, new PlanetDimensionEffects());
    }
}