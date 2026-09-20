package shipwrights.genesis.mixin;

import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper", remap = false)
public interface RebindableTickingBlockEntityWrapperInvoker {

    @Invoker(value = "rebind", remap = false)
    void invokeRebind(TickingBlockEntity ticker);
}