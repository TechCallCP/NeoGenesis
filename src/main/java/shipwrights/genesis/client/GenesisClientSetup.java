package shipwrights.genesis.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import org.jetbrains.annotations.NotNull;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.client.blockentityRenderer.NavProjectorBlockEntityRenderer;
import shipwrights.genesis.client.blockentityRenderer.RadarDisplayBlockEntityRenderer;
import shipwrights.genesis.client.blockentityRenderer.VoidCoreBlockEntityRenderer;
import shipwrights.genesis.client.blockentityRenderer.VoidEngineInterfaceBlockEntityRenderer;
import shipwrights.genesis.content.block.GenesisBlocks;
import shipwrights.genesis.content.blockentity.GenesisBlockEntities;
import shipwrights.genesis.content.particle.GenesisParticles;
import shipwrights.genesis.content.particle.VerditeParticle;
import shipwrights.genesis.content.particle.ZapBubbleParticle;

public class GenesisClientSetup {

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(GenesisBlockEntities.NAV_PROJECTOR.get(), NavProjectorBlockEntityRenderer::new);
            BlockEntityRenderers.register(GenesisBlockEntities.RADAR_DISPLAY.get(), RadarDisplayBlockEntityRenderer::new);
            BlockEntityRenderers.register(GenesisBlockEntities.VOID_CORE.get(), VoidCoreBlockEntityRenderer::new);
            BlockEntityRenderers.register(GenesisBlockEntities.VOID_ENGINE_INTERFACE.get(), VoidEngineInterfaceBlockEntityRenderer::new);
        });
    }

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(GenesisBlocks.TULCITE_CATALYZER_CONTAINER.get(), TulciteCatalyzerScreen::new);
    }

    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected @NotNull Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(@NotNull Void object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                ShaderRegistry.clearTexturedPlanetRenderTypes();
                NeoGenesisMod.LOGGER.debug("Cleared planet texture caches");
            }
        });
    }

    public static void registerParticleProvider(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(GenesisParticles.ZAP_BUBBLE_PARTICLES.get(), ZapBubbleParticle.Provider::new);
        event.registerSpriteSet(GenesisParticles.VERDITE_PARTICLES.get(), VerditeParticle.Provider::new);
    }
}