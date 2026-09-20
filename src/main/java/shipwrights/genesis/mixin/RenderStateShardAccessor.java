package shipwrights.genesis.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {

    @Accessor(value = "TRANSLUCENT_TARGET", remap = false)
    static RenderStateShard.OutputStateShard getTRANSLUCENT_TARGET() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "VIEW_OFFSET_Z_LAYERING", remap = false)
    static RenderStateShard.LayeringStateShard getVIEW_OFFSET_Z_LAYERING() {
        throw new UnsupportedOperationException();
    }
}