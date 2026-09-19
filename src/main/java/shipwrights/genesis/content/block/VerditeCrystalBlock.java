package shipwrights.genesis.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import shipwrights.genesis.content.particle.GenesisParticles;

public class VerditeCrystalBlock extends AmethystBlock {
    public static final MapCodec<VerditeCrystalBlock> CODEC = simpleCodec(VerditeCrystalBlock::new);
    public static final int GROWTH_CHANCE = 10;
    private static final Direction[] DIRECTIONS = Direction.values();

    public VerditeCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<VerditeCrystalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.dayTime() > 0 && level.dayTime() < 12000) {
            if (random.nextInt(GROWTH_CHANCE) == 0) {
                Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                BlockPos blockPos = pos.relative(direction);
                BlockState blockState = level.getBlockState(blockPos);
                Block block = null;

                if (canClusterGrowAtState(blockState)) {
                    block = GenesisBlocks.SMALL_VERDITE_BUD.get();
                } else if (blockState.is(GenesisBlocks.SMALL_VERDITE_BUD.get()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                    block = GenesisBlocks.MEDIUM_VERDITE_BUD.get();
                } else if (blockState.is(GenesisBlocks.MEDIUM_VERDITE_BUD.get()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                    block = GenesisBlocks.LARGE_VERDITE_BUD.get();
                } else if (blockState.is(GenesisBlocks.LARGE_VERDITE_BUD.get()) && blockState.getValue(AmethystClusterBlock.FACING) == direction) {
                    block = GenesisBlocks.VERDITE_CLUSTER.get();
                }

                if (block != null) {
                    BlockState blockState2 = block.defaultBlockState()
                            .setValue(AmethystClusterBlock.FACING, direction)
                            .setValue(AmethystClusterBlock.WATERLOGGED, blockState.getFluidState().getType() == Fluids.WATER);
                    level.setBlockAndUpdate(blockPos, blockState2);
                }

            } else if (random.nextInt(GROWTH_CHANCE) == 1) {
                Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
                BlockPos blockPos = pos.relative(direction);
                BlockState blockState = level.getBlockState(blockPos);
                Block block = null;

                if (blockState.is(GenesisBlocks.RIFTROCK.get())) {
                    block = GenesisBlocks.WARPSTONE.get();
                    level.playSound((Player) null, blockPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 1.2F);
                } else if (blockState.is(GenesisBlocks.WARPSTONE.get())) {
                    block = GenesisBlocks.VERDITE_ORE.get();
                    level.playSound((Player) null, blockPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 1.2F);
                } else if (blockState.is(GenesisBlocks.VERDITE_ORE.get())) {
                    block = GenesisBlocks.VERDITE_CRYSTAL_BLOCK.get();
                    level.playSound((Player) null, blockPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 1.2F);
                }

                if (block != null) {
                    level.setBlockAndUpdate(blockPos, block.defaultBlockState());
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) == 0) {
            spawnParticles(level, pos);
        }
    }

    private static void spawnParticles(Level level, BlockPos pos) {
        RandomSource random = level.random;

        for (Direction direction : Direction.values()) {
            BlockPos relativePos = pos.relative(direction);
            if (!level.getBlockState(relativePos).isSolidRender(level, relativePos)) {
                Direction.Axis axis = direction.getAxis();
                double d1 = axis == Direction.Axis.X ? 0.5D + 0.5625D * direction.getStepX() : random.nextFloat();
                double d2 = axis == Direction.Axis.Y ? 0.5D + 0.5625D * direction.getStepY() : random.nextFloat();
                double d3 = axis == Direction.Axis.Z ? 0.5D + 0.5625D * direction.getStepZ() : random.nextFloat();
                level.addParticle(GenesisParticles.VERDITE_PARTICLES.get(), pos.getX() + d1, pos.getY() + d2, pos.getZ() + d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || (state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8);
    }
}