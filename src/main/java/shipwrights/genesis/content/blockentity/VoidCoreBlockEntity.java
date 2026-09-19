package shipwrights.genesis.content.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.content.block.GenesisBlocks;
import shipwrights.genesis.content.block.VoidCoreBlock;

import java.util.List;

public class VoidCoreBlockEntity extends BlockEntity {

    public VoidCoreBlockEntity(BlockPos pos, BlockState state) {
        super(GenesisBlockEntities.VOID_CORE.get(), pos, state);
    }

    public List<Block> frameBlocks = List.of(
            GenesisBlocks.VOID_ENGINE_FRAME.get(),
            GenesisBlocks.VOID_CORE_REFLECTOR_PANEL.get(),
            GenesisBlocks.VOID_ENGINE_VIEWPORT.get()
    );

    public List<Block> focusBlocks = List.of(
            GenesisBlocks.VOID_FOCUS.get()
    );

    public static void updateVoidCore(BlockPos framePos, LevelReader level) {
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    BlockEntity blockEntity = level.getBlockEntity(framePos.offset(x, y, z));
                    if (blockEntity instanceof VoidCoreBlockEntity voidCore) {
                        voidCore.updateDormancy(framePos);
                        return;
                    }
                }
            }
        }
    }

    private void updateDormancy(BlockPos pos) {
        if (level == null) return;
        int frames = 0;
        int interfaces = 0;
        int focuses = 0;

        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    BlockPos framePos = this.getBlockPos().offset(x, y, z);
                    if (frameBlocks.stream().anyMatch(level.getBlockState(framePos)::is)) {
                        frames++;
                    } else if (level.getBlockState(framePos).is(GenesisBlocks.VOID_ENGINE_INTERFACE.get())) {
                        interfaces++;
                    } else if (focusBlocks.stream().anyMatch(level.getBlockState(framePos)::is)) {
                        focuses++;
                    }
                }
            }
        }

        int neededFrames = 25;
        if (focuses == 1) {
            neededFrames = 24;
        } else if (focuses == 2) {
            neededFrames = 16;
        } else if (focuses > 2) {
            neededFrames = 0;
        }

        if (frames >= neededFrames && interfaces == 1) {
            level.setBlock(this.getBlockPos(), GenesisBlocks.VOID_CORE.get().defaultBlockState().setValue(VoidCoreBlock.DORMANT, false), Block.UPDATE_CLIENTS);
        } else {
            level.setBlock(this.getBlockPos(), GenesisBlocks.VOID_CORE.get().defaultBlockState().setValue(VoidCoreBlock.DORMANT, true), Block.UPDATE_CLIENTS);
            if (level.dimension().location().equals(NeoGenesisMod.WORMHOLE_DIM) && level.getServer() != null) {
                ServerLevel returnLevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "great_unknown")));
                VoidEngineInterfaceBlockEntity.returnFromWormhole(level, pos, returnLevel, true);
            }
        }
    }
}
