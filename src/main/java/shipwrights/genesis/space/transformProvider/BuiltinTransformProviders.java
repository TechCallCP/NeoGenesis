package shipwrights.genesis.space.transformProvider;

import net.neoforged.bus.api.IEventBus;

public class BuiltinTransformProviders {

    public static void register(IEventBus modEventBus) {
        StaticTransformProvider.register(modEventBus);
        OrbitingTransformProvider.register(modEventBus);
    }
}