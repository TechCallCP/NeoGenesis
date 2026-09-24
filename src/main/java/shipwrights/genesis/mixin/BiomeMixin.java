package shipwrights.genesis.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.util.ShipUtils;

@Mixin(value = Biome.class, remap = false)
public abstract class BiomeMixin {

    @Inject(method = "shouldSnow", at = @At("HEAD"), cancellable = true)
    private void genesis$shouldSnow(LevelReader levelReader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (levelReader instanceof ServerLevel level) {
            if (NeoGenesisMod.shouldCancelVoidDamage(level) || ShipUtils.isPosInHighShip(level, pos)) {
                cir.setReturnValue(false);
            }
        }
    }
}