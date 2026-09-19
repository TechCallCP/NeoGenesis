package shipwrights.genesis.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import shipwrights.genesis.content.blockentity.VoidCoreBlockEntity;

public class VoidEngineViewportBlock extends TransparentBlock {
    public static final MapCodec<VoidEngineViewportBlock> CODEC = simpleCodec(VoidEngineViewportBlock::new);

    public VoidEngineViewportBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public VoidEngineViewportBlock() {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                .strength(3.0f)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    protected MapCodec<VoidEngineViewportBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        VoidCoreBlockEntity.updateVoidCore(pos, level);
    }
}
