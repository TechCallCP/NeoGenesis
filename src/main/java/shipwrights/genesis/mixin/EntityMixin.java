package shipwrights.genesis.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import shipwrights.genesis.NeoGenesisMod;

@Mixin(value = Entity.class, remap = false)
public abstract class EntityMixin {

    @Shadow
    public abstract Level level();

    @Inject(method = "tick", at = @At("HEAD"))
    private void genesis$onTick(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level() instanceof ServerLevel serverLevel) {
            NeoGenesisMod.refreshEntityScaling(self, serverLevel);
        }
    }

    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void genesis$isInWater(CallbackInfoReturnable<Boolean> cir) {
        if (NeoGenesisMod.isSpaceDimension(this.level())) {
            cir.setReturnValue(false);
        }
    }
}