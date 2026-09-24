package shipwrights.genesis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiPlayerGameMode.class, remap = false)
public class MultiPlayerGameModeMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void neogenesis$guardNullPlayerTick(CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) {
            ci.cancel();
        }
    }
}