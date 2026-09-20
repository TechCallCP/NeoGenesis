package shipwrights.genesis.client;

import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

import shipwrights.genesis.NeoGenesisMod;

import static shipwrights.genesis.NeoGenesisMod.*;

public class DimensionSpecialEffectsManagerMixin {

    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(SPACE_DIM, new SpaceDimensionEffects());
        event.register(WORMHOLE_DIM, new WormholeDimensionEffects());
        event.register(GENERIC_PLANET_ID, new PlanetDimensionEffects());
    }
}