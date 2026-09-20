package shipwrights.genesis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.client.*;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    private static Minecraft instance;

    @Unique
    private static boolean genesis$settingTransition = false;

    @Inject(method = "setLevel", at = @At("HEAD"), remap = false)
    private void setLevelInject(ClientLevel newLevel, CallbackInfo ci) {
        ClientLevel oldLevel = instance.level;

        if (oldLevel == null) {
            TransitionState.CURRENT = TransitionState.NONE;
        } else if (NeoGenesisMod.isSubSpaceDimension(oldLevel) || NeoGenesisMod.isSubSpaceDimension(newLevel)) {
            TransitionState.CURRENT = TransitionState.WORMHOLE_TRAVEL;
        } else if (NeoGenesisMod.isSpaceDimension(oldLevel) || NeoGenesisMod.isSpaceDimension(newLevel)) {
            TransitionState.CURRENT = TransitionState.SPACE_TRAVEL;
        } else {
            TransitionState.CURRENT = TransitionState.NONE;
        }
    }

    @Inject(method = "setScreen", at = @At("RETURN"), remap = false)
    private void setScreenInject(Screen screen, CallbackInfo ci) {
        if (genesis$settingTransition || TransitionState.CURRENT == TransitionState.NONE) return;

        if (screen == null || screen instanceof WarpLoadingMenu || screen instanceof TransitionScreen) {
            return;
        }

        if (screen instanceof ReceivingLevelScreen || screen instanceof ProgressScreen) {
            if (TransitionState.CURRENT == TransitionState.SPACE_TRAVEL || TransitionState.CURRENT == TransitionState.WORMHOLE_TRAVEL) {
                genesis$settingTransition = true;
                TransitionFrame.captureFrame();
                instance.setScreen(new TransitionScreen());
                genesis$settingTransition = false;
            }
        }
    }
}