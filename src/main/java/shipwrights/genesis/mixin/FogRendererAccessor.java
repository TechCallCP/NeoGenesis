package shipwrights.genesis.mixin;

import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FogRenderer.class)
public interface FogRendererAccessor {

    @Accessor(value = "fogRed", remap = false)
    static float getFogRed() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "fogGreen", remap = false)
    static float getFogGreen() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "fogBlue", remap = false)
    static float getFogBlue() {
        throw new UnsupportedOperationException();
    }
}