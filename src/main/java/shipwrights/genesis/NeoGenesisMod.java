package shipwrights.genesis;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

import org.slf4j.Logger;

import shipwrights.genesis.client.DimensionSpecialEffectsManagerMixin;
import shipwrights.genesis.client.GenesisClientSetup;
import shipwrights.genesis.commands.NeogenesisCommandArguments;
import shipwrights.genesis.content.block.GenesisBlocks;
import shipwrights.genesis.content.blockentity.GenesisBlockEntities;
import shipwrights.genesis.content.fluid.GenesisFluids;
import shipwrights.genesis.content.item.GenesisCreativeTabs;
import shipwrights.genesis.content.item.GenesisItems;
import shipwrights.genesis.content.painting.GenesisPaintings;
import shipwrights.genesis.content.particle.GenesisParticles;
import shipwrights.genesis.content.sound.GenesisSounds;
import shipwrights.genesis.handler.SableEntitySpaceHandler;
import shipwrights.genesis.networking.GenesisNetworking;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.type.BuiltinCelestialTypes;
import shipwrights.genesis.worldgen.WorldGenRegistry;

@Mod(NeoGenesisMod.MOD_ID)
public class NeoGenesisMod {

    public static final String MOD_ID = "neogenesis";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation SPACE_DIM = ResourceLocation.fromNamespaceAndPath(MOD_ID, "space");
    public static final ResourceLocation WORMHOLE_DIM = ResourceLocation.fromNamespaceAndPath(MOD_ID, "wormhole");
    public static final ResourceLocation GENERIC_PLANET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "planet");

    public static final ResourceKey<Registry<Celestial>> CELESTIAL_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "celestial"));

    public static long clientTimeOffset = 0;

    public NeoGenesisMod(IEventBus modEventBus) {
        GenesisNetworking.init(modEventBus);
        WorldGenRegistry.init(modEventBus);

        // Command argument registry
        NeogenesisCommandArguments.register(modEventBus);

        // All DeferredRegisters connected to the mod event bus
        GenesisBlocks.register(modEventBus);
        GenesisItems.register(modEventBus);
        GenesisCreativeTabs.register(modEventBus); // <-- Registers your creative tabs
        GenesisBlockEntities.register(modEventBus);
        GenesisFluids.register(modEventBus);
        GenesisParticles.register(modEventBus);
        GenesisPaintings.register(modEventBus);
        GenesisSounds.register(modEventBus);

        BuiltinCelestialTypes.register();

        NeoForge.EVENT_BUS.register(SableEntitySpaceHandler.class);

        modEventBus.addListener(this::commonSetup);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(GenesisClientSetup::onClientSetup);
            modEventBus.addListener(GenesisClientSetup::onRegisterMenuScreens);
            modEventBus.addListener(GenesisClientSetup::onRegisterReloadListeners);
            modEventBus.addListener(GenesisClientSetup::registerParticleProvider);
            modEventBus.addListener(DimensionSpecialEffectsManagerMixin::registerDimensionEffects);
        }
    }

    public static boolean isMiniScale(ServerLevel level) {
        return false;
    }

    public static boolean isMiniScale(Level level) {
        return false;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }

    public static boolean isSpaceDimension(Level level) {
        return level != null && level.dimension().location().getNamespace().equals(MOD_ID)
                && level.dimension().location().getPath().contains("space");
    }

    public static boolean isSubspaceDimension(Level level) {
        return level != null && level.dimension().location().getNamespace().equals(MOD_ID)
                && level.dimension().location().getPath().contains("wormhole");
    }

    public static boolean shouldCancelVoidDamage(Level level) {
        return isSpaceDimension(level);
    }

    public static void refreshEntityScaling(Entity entity, Level level) {
        if (entity == null || level == null) return;
        if (isSpaceDimension(level)) {
            entity.setNoGravity(true);
        }
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