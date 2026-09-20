package shipwrights.genesis.mixin.compat.mekanism;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import shipwrights.genesis.NeoGenesisMod;

import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator", remap = false)
public abstract class AdvancedSolarGeneratorMixin {

    @Inject(method = "checkCanSeeSun", at = @At("HEAD"), cancellable = true)
    private void genesis$checkCanSeeSun(CallbackInfoReturnable<Boolean> cir) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null && NeoGenesisMod.isSpaceDimension(level)) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    @Inject(method = "getProduction", at = @At("HEAD"), cancellable = true)
    private void genesis$getProduction(CallbackInfoReturnable<Object> cir) {
        Level level = ((BlockEntity) (Object) this).getLevel();
        if (level != null && NeoGenesisMod.isSpaceDimension(level)) {
            Object max = genesis$getConfiguredMax();
            if (max != null) {
                cir.setReturnValue(max);
            }
        }
    }

    private Object genesis$getConfiguredMax() {
        try {
            Method method = this.getClass().getMethod("getConfiguredMax");
            return method.invoke(this);
        } catch (Exception e) {
            return null;
        }
    }
}