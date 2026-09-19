package shipwrights.genesis.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.config.GenesisClientConfig;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoGenesisMod.MOD_ID)
public class SpaceShaderEventHandler {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level != null) {
            boolean shouldBeActive = NeoGenesisMod.isSpaceDimension(level) && GenesisClientConfig.enableSpaceLighting();
            SpaceInvertPostProcessor.INSTANCE.setActive(shouldBeActive);
        }
    }
}
