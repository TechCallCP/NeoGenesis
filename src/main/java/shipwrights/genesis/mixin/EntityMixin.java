package shipwrights.genesis.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import shipwrights.genesis.NeoGenesisMod;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow(remap = false)
    public abstract Level level();

    @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true, remap = false)
    private void onBelowWorldMixin(CallbackInfo ci) {
        if (NeoGenesisMod.shouldCancelVoidDamage(this.level())) {
            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void mixinEntityInit(EntityType<?> entityType, Level level, CallbackInfo ci) {
        NeoGenesisMod.refreshEntityScaling((Entity) (Object) this, level);
    }
}