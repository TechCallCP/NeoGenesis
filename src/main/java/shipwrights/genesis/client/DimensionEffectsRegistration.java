package shipwrights.genesis.client;

import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

import shipwrights.genesis.NeoGenesisMod;

public class DimensionEffectsRegistration {

    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(NeoGenesisMod.SPACE_DIM, new SpaceDimensionEffects());
        event.register(NeoGenesisMod.WORMHOLE_DIM, new WormholeDimensionEffects());
        event.register(NeoGenesisMod.GENERIC_PLANET_ID, new PlanetDimensionEffects());
    }
}
