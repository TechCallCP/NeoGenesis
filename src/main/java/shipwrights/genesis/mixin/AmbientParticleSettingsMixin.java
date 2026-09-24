package shipwrights.genesis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AmbientParticleSettings.class)
public abstract class AmbientParticleSettingsMixin {

    @Inject(method = "canSpawn", at = @At("RETURN"), cancellable = true, remap = false)
    private void genesis$fadeBiomeParticles(RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;

        Minecraft mc = Minecraft.getInstance();
        // Null-check mc to prevent early boot NPEs before game renderer initializes
        if (mc == null || mc.gameRenderer == null || mc.gameRenderer.getMainCamera() == null) return;

        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double densityFade = 1.0 - Mth.clamp((camY - 300.0) / 60.0, 0.0, 1.0);

        if (densityFade < 1.0) {
            if (densityFade <= 0.0 || random.nextFloat() >= densityFade) {
                cir.setReturnValue(false);
            }
        }
    }
}