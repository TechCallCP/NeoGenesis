//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package shipwrights.genesis.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;

import shipwrights.genesis.content.GenesisTags;

public class BrineTrunkPlantBlock extends PipeBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public BrineTrunkPlantBlock(BlockBehaviour.Properties properties) {
        super(0.3125F, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(BlockGetter level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        BlockState belowState = level.getBlockState(pos.below());
        BlockState aboveState = level.getBlockState(pos.above());
        BlockState northState = level.getBlockState(pos.north());
        BlockState eastState = level.getBlockState(pos.east());
        BlockState southState = level.getBlockState(pos.south());
        BlockState westState = level.getBlockState(pos.west());

        return this.defaultBlockState()
                .setValue(DOWN, belowState.is(this) || belowState.is(GenesisBlocks.BRINE_FLOWER.get()) || belowState.is(GenesisTags.Blocks.BRINE_TRUNK_PLANTABLE))
                .setValue(UP, aboveState.is(this) || aboveState.is(GenesisBlocks.BRINE_FLOWER.get()))
                .setValue(NORTH, northState.is(this) || northState.is(GenesisBlocks.BRINE_FLOWER.get()))
                .setValue(EAST, eastState.is(this) || eastState.is(GenesisBlocks.BRINE_FLOWER.get()))
                .setValue(SOUTH, southState.is(this) || southState.is(GenesisBlocks.BRINE_FLOWER.get()))
                .setValue(WEST, westState.is(this) || westState.is(GenesisBlocks.BRINE_FLOWER.get()))
                .setValue(WATERLOGGED, fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            level.scheduleTick(currentPos, this, 1);
            return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        } else {
            boolean flag = neighborState.is(this) || neighborState.is(GenesisBlocks.BRINE_FLOWER.get()) || (direction == Direction.DOWN && neighborState.is(GenesisTags.Blocks.BRINE_TRUNK_PLANTABLE));
            return state.setValue(PROPERTY_BY_DIRECTION.get(direction), flag);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        boolean flag = !level.getBlockState(pos.above()).isAir() && !belowState.isAir();

        for (Direction direction : Plane.HORIZONTAL) {
            BlockPos relativePos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(relativePos);
            if (neighborState.is(this)) {
                if (flag) {
                    return false;
                }

                BlockState neighborBelow = level.getBlockState(relativePos.below());
                if (neighborBelow.is(this) || neighborBelow.is(GenesisTags.Blocks.BRINE_TRUNK_PLANTABLE)) {
                    return true;
                }
            }
        }

        return belowState.is(this) || belowState.is(GenesisTags.Blocks.BRINE_TRUNK_PLANTABLE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}