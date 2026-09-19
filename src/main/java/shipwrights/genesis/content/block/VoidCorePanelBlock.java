package shipwrights.genesis.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import shipwrights.genesis.content.blockentity.VoidCoreBlockEntity;

public class VoidCorePanelBlock extends Block {
    public static final MapCodec<VoidCorePanelBlock> CODEC = simpleCodec(VoidCorePanelBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;

    private static final VoxelShape SQUARE_COLLIDER = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_COLLIDER = Block.box(0.0D, 0.0D, 3.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_COLLIDER = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 13.0D);
    private static final VoxelShape EAST_COLLIDER = Block.box(0.0D, 0.0D, 0.0D, 13.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_COLLIDER = Block.box(3.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape UP_COLLIDER = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);
    private static final VoxelShape DOWN_COLLIDER = Block.box(0.0D, 3.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public VoidCorePanelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ATTACHED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getNearestLookingDirection().getOpposite();
        if (context.getPlayer() != null && context.getPlayer().isCrouching()) {
            facing = facing.getOpposite();
        }
        BlockPos frontPos = context.getClickedPos().relative(facing);

        BlockState frontState = context.getLevel().getBlockState(frontPos);
        if (frontState.isAir()) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(ATTACHED, false);
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(ATTACHED, true);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);
        if (!neighborPos.equals(pos.relative(facing))) return;

        boolean blocked = !level.getBlockState(neighborPos).isAir();
        if (state.getValue(ATTACHED) != blocked) {
            level.setBlock(pos, state.setValue(ATTACHED, blocked), 3);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(ATTACHED)) {
            return SQUARE_COLLIDER;
        }

        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case NORTH -> NORTH_COLLIDER;
            case SOUTH -> SOUTH_COLLIDER;
            case EAST -> EAST_COLLIDER;
            case WEST -> WEST_COLLIDER;
            case UP -> UP_COLLIDER;
            case DOWN -> DOWN_COLLIDER;
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        VoidCoreBlockEntity.updateVoidCore(pos, level);
    }
}
