package shipwrights.genesis.mixin;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModConfigSpec.ConfigValue.class, remap = false)
public abstract class ConfigValueMixin {

    @Shadow
    private ModConfigSpec spec;

    @Shadow
    public abstract Object getDefault();

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void neogenesis$fallbackDefaultOnUnloadedConfig(CallbackInfoReturnable<Object> cir) {
        if (this.spec == null || !this.spec.isLoaded()) {
            cir.setReturnValue(this.getDefault());
        }
    }
}