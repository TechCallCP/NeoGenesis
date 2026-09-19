package shipwrights.genesis.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

import shipwrights.genesis.NeoGenesisMod;

@EventBusSubscriber(modid = NeoGenesisMod.MOD_ID, value = Dist.CLIENT)
public class DimensionEffectsRegistration {

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(NeoGenesisMod.SPACE_DIM, new SpaceDimensionEffects());
        event.register(NeoGenesisMod.WORMHOLE_DIM, new WormholeDimensionEffects());
        event.register(NeoGenesisMod.GENERIC_PLANET_ID, new PlanetDimensionEffects());
    }
}
