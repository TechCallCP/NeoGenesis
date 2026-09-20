package shipwrights.genesis.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import shipwrights.genesis.NeoGenesisMod;

public class WorldGenRegistry {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(WorldGenRegistry::onRegisterEvent);
    }

    private static void onRegisterEvent(RegisterEvent event) {
        event.register(Registries.DENSITY_FUNCTION_TYPE, helper -> {
            helper.register(
                    ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "random_noise"),
                    RandomNoise.KEY_CODEC.codec()
            );
            helper.register(
                    ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "asteroid_belt"),
                    AsteroidBelt.MAP_CODEC
            );
            helper.register(
                    ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "radial_gradient"),
                    RadialGradientDensity.MAP_CODEC
            );
            helper.register(
                    CraterNoise.RESOURCE_LOCATION,
                    CraterNoise.CODEC.codec()
            );
            helper.register(
                    MultiCraterNoise.RESOURCE_LOCATION,
                    MultiCraterNoise.CODEC.codec()
            );
        });

        event.register(Registries.MATERIAL_RULE, helper -> {
            helper.register(
                    ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "asteroid_rule"),
                    AsteroidBlockSurfaceRule.CODEC.codec()
            );
        });
    }
}
