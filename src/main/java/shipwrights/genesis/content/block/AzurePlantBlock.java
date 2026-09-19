package shipwrights.genesis.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import shipwrights.genesis.content.GenesisTags;

public class AzurePlantBlock extends BushBlock {
    public static final MapCodec<AzurePlantBlock> CODEC = simpleCodec(AzurePlantBlock::new);

    public AzurePlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<AzurePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(GenesisTags.Blocks.BLUE_MOSSES);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return pathComputationType == PathComputationType.AIR && !this.hasCollision ? true : super.isPathfindable(state, pathComputationType);
    }
}