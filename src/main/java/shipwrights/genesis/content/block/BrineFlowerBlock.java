package shipwrights.genesis.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.event.EventHooks;

import shipwrights.genesis.content.GenesisTags;

import javax.annotation.Nullable;

public class BrineFlowerBlock extends Block {
    public static final int DEAD_AGE = 5;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    private final BrineTrunkPlantBlock plant;

    public BrineFlowerBlock(BrineTrunkPlantBlock plant, BlockBehaviour.Properties properties) {
        super(properties);
        this.plant = plant;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 5;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        if (level.isEmptyBlock(abovePos) && abovePos.getY() < level.getMaxBuildHeight()) {
            int age = state.getValue(AGE);
            if (age < 5 && EventHooks.onCropsGrowPre(level, abovePos, state, true)) {
                boolean flag = false;
                boolean flag1 = false;
                BlockState belowState = level.getBlockState(pos.below());
                if (!belowState.is(GenesisTags.Blocks.SALT_PLANTABLE)) {
                    if (!belowState.is(this.plant)) {
                        if (belowState.isAir()) {
                            flag = true;
                        }
                    } else {
                        int j = 1;

                        for (int k = 0; k < 4; ++k) {
                            BlockState state1 = level.getBlockState(pos.below(j + 1));
                            if (!state1.is(this.plant)) {
                                if (state1.is(GenesisTags.Blocks.BRINE_TRUNK_PLANTABLE)) {
                                    flag1 = true;
                                }
                                break;
                            }

                            ++j;
                        }

                        if (j < 2 || j <= random.nextInt(flag1 ? 5 : 4)) {
                            flag = true;
                        }
                    }
                } else {
                    flag = true;
                }

                if (flag && allNeighborsEmpty(level, abovePos, null) && level.isEmptyBlock(pos.above(2))) {
                    level.setBlock(pos, this.plant.getStateForPlacement(level, pos), 2);
                    this.placeGrownFlower(level, abovePos, age);
                } else if (age >= 4) {
                    this.placeDeadFlower(level, pos);
                } else {
                    int l = random.nextInt(4);
                    if (flag1) {
                        ++l;
                    }

                    boolean flag2 = false;

                    for (int i1 = 0; i1 < l; ++i1) {
                        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                        BlockPos targetPos = pos.relative(direction);
                        if (level.isEmptyBlock(targetPos) && level.isEmptyBlock(targetPos.below()) && allNeighborsEmpty(level, targetPos, direction.getOpposite())) {
                            this.placeGrownFlower(level, targetPos, age + 1);
                            flag2 = true;
                        }
                    }

                    if (flag2) {
                        level.setBlock(pos, this.plant.getStateForPlacement(level, pos), 2);
                    } else {
                        this.placeDeadFlower(level, pos);
                    }
                }

                EventHooks.onCropsGrowPost(level, pos, state);
            }
        }
    }

    private void placeGrownFlower(Level level, BlockPos pos, int age) {
        level.setBlock(pos, this.defaultBlockState().setValue(AGE, age), 2);
        level.levelEvent(1033, pos, 0);
    }

    private void placeDeadFlower(Level level, BlockPos pos) {
        level.setBlock(pos, this.defaultBlockState().setValue(AGE, 5), 2);
        level.levelEvent(1034, pos, 0);
    }

    private static boolean allNeighborsEmpty(LevelReader level, BlockPos pos, @Nullable Direction ignoreDirection) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction != ignoreDirection && !level.isEmptyBlock(pos.relative(direction))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction != Direction.UP && !state.canSurvive(level, currentPos)) {
            level.scheduleTick(currentPos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        if (!belowState.is(this.plant) && !belowState.is(GenesisTags.Blocks.BRINE_TRUNK_PLANTABLE)) {
            if (!belowState.isAir()) {
                return false;
            } else {
                boolean flag = false;

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockState neighborState = level.getBlockState(pos.relative(direction));
                    if (neighborState.is(this.plant)) {
                        if (flag) {
                            return false;
                        }
                        flag = true;
                    } else if (!neighborState.isAir()) {
                        return false;
                    }
                }

                return flag;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public static void generatePlant(LevelAccessor level, BlockPos pos, RandomSource random, int i) {
        level.setBlock(pos, ((BrineTrunkPlantBlock) GenesisBlocks.BRINE_TRUNK.get()).getStateForPlacement(level, pos), 2);
        growTreeRecursive(level, pos, random, pos, i, 0);
    }

    private static void growTreeRecursive(LevelAccessor level, BlockPos pos, RandomSource random, BlockPos origin, int m, int n) {
        BrineTrunkPlantBlock trunk = (BrineTrunkPlantBlock) GenesisBlocks.BRINE_TRUNK.get();
        int i = random.nextInt(4) + 1;
        if (n == 0) {
            ++i;
        }

        for (int j = 0; j < i; ++j) {
            BlockPos abovePos = pos.above(j + 1);
            if (!allNeighborsEmpty(level, abovePos, null)) {
                return;
            }

            level.setBlock(abovePos, trunk.getStateForPlacement(level, abovePos), 2);
            level.setBlock(abovePos.below(), trunk.getStateForPlacement(level, abovePos.below()), 2);
        }

        boolean flag = false;
        if (n < 4) {
            int l = random.nextInt(4);
            if (n == 0) {
                ++l;
            }

            for (int k = 0; k < l; ++k) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos targetPos = pos.above(i).relative(direction);
                if (Math.abs(targetPos.getX() - origin.getX()) < m && Math.abs(targetPos.getZ() - origin.getZ()) < m && level.isEmptyBlock(targetPos) && level.isEmptyBlock(targetPos.below()) && allNeighborsEmpty(level, targetPos, direction.getOpposite())) {
                    flag = true;
                    level.setBlock(targetPos, trunk.getStateForPlacement(level, targetPos), 2);
                    level.setBlock(targetPos.relative(direction.getOpposite()), trunk.getStateForPlacement(level, targetPos.relative(direction.getOpposite())), 2);
                    growTreeRecursive(level, targetPos, random, origin, m, n + 1);
                }
            }
        }

        if (!flag) {
            level.setBlock(pos.above(i), GenesisBlocks.BRINE_FLOWER.get().defaultBlockState().setValue(AGE, 5), 2);
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hitResult, Projectile projectile) {
        BlockPos pos = hitResult.getBlockPos();
        if (!level.isClientSide && projectile.mayInteract(level, pos) && projectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
            level.destroyBlock(pos, true, projectile);
        }
    }
}