package shipwrights.genesis.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import shipwrights.genesis.content.blockentity.VoidCoreBlockEntity;

public class VoidEngineFrameBlock extends Block {
    public static final MapCodec<VoidEngineFrameBlock> CODEC = simpleCodec(VoidEngineFrameBlock::new);

    public VoidEngineFrameBlock(Properties properties) {
        super(properties);
    }

    public VoidEngineFrameBlock() {
        this(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .strength(3.0f)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        VoidCoreBlockEntity.updateVoidCore(pos, level);
    }
}
