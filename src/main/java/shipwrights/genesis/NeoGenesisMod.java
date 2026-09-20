package shipwrights.genesis;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import shipwrights.genesis.content.fluid.GenesisFluids;
import shipwrights.genesis.networking.GenesisNetworking;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.type.BuiltinCelestialTypes;
import shipwrights.genesis.worldgen.WorldGenRegistry;

@Mod(NeoGenesisMod.MOD_ID)
public class NeoGenesisMod {

    public static final String MOD_ID = "genesis";
    public static final ResourceKey<Registry<Celestial>> CELESTIAL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "celestial"));

    public static long clientTimeOffset = 0;

    public NeoGenesisMod(IEventBus modEventBus) {
        // Pass the modEventBus argument required by updated subsystem initializers
        GenesisNetworking.init(modEventBus);
        WorldGenRegistry.init(modEventBus);
        GenesisFluids.register(modEventBus);

        BuiltinCelestialTypes.register();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common initialization logic
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Client initialization logic
    }

    public static boolean isSpaceDimension(Level level) {
        return level.dimension().location().getNamespace().equals(MOD_ID)
                && level.dimension().location().getPath().contains("space");
    }

    public static Celestial getCelestialForLevel(Level level) {
        Registry<Celestial> registry = getCelestialRegistry(level);
        if (registry == null) return null;
        return registry.get(ResourceLocation.fromNamespaceAndPath(MOD_ID, level.dimension().location().getPath()));
    }

    public static Registry<Celestial> getCelestialRegistry(Level level) {
        return level.registryAccess().registry(CELESTIAL_REGISTRY_KEY).orElse(null);
    }

    public static long getTicks(Level level) {
        return level.getGameTime() + clientTimeOffset;
    }

    public static float getPartialTick(Level level, RenderLevelStageEvent event) {
        return event.getPartialTick().getGameTimeDeltaPartialTick(true);
    }

    public static double getDimensionScale(ServerLevel level) {
        return 1.0;
    }
}