package shipwrights.genesis.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import shipwrights.genesis.content.GenesisTags;

public class ColoredSaltPlantBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 4);
    private static final VoxelShape BOX_COLLIDER = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public ColoredSaltPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(COLOR, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(BlockGetter level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.is(GenesisBlocks.SALT.get()) || belowState.is(GenesisBlocks.CRACKED_SALT.get())) {
            return this.defaultBlockState().setValue(COLOR, 0);
        } else if (belowState.is(GenesisBlocks.PALE_RED_SALT.get()) || belowState.is(GenesisBlocks.CRACKED_PALE_RED_SALT.get())) {
            return this.defaultBlockState().setValue(COLOR, 1);
        } else if (belowState.is(GenesisBlocks.RED_SALT.get()) || belowState.is(GenesisBlocks.CRACKED_RED_SALT.get())) {
            return this.defaultBlockState().setValue(COLOR, 2);
        } else if (belowState.is(GenesisBlocks.TURQUOISE_SALT.get()) || belowState.is(GenesisBlocks.CRACKED_TURQUOISE_SALT.get())) {
            return this.defaultBlockState().setValue(COLOR, 3);
        } else if (belowState.is(GenesisBlocks.CYAN_SALT.get()) || belowState.is(GenesisBlocks.CRACKED_CYAN_SALT.get())) {
            return this.defaultBlockState().setValue(COLOR, 4);
        }
        return this.defaultBlockState().setValue(COLOR, 2);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            level.scheduleTick(currentPos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
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
        return belowState.is(GenesisTags.Blocks.SALT_PLANTABLE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, COLOR);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BOX_COLLIDER;
    }
}
