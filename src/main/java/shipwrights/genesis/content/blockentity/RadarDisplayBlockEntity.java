package shipwrights.genesis.content.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import shipwrights.genesis.content.block.RadarDisplayBlock;
import shipwrights.genesis.content.radar.RadarDisplay;

import java.util.ArrayList;
import java.util.List;

public class RadarDisplayBlockEntity extends BlockEntity {

    public RadarDisplay display = new RadarDisplay(64);

    public RadarDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(GenesisBlockEntities.RADAR_DISPLAY.get(), pos, state);
    }

    public double[][] getDisplayableData() {
        return display.data;
    }

    public void clientTick() {
        if (level == null) return;
        BlockState state = level.getBlockState(getBlockPos());
        if (state.getBlock() instanceof RadarDisplayBlock) {
            Vec3 center = getBlockPos().getCenter();
            Vec3i normal = state.getValue(RadarDisplayBlock.FACING).getOpposite().getNormal();

            Vector3d pos = new Vector3d(center.x, center.y, center.z);
            Vector3d dir = new Vector3d(normal.getX(), normal.getY(), normal.getZ());
            Vector3d up = getUpVectorForFacing(state.getValue(RadarDisplayBlock.FACING));

            List<Long> excludedEntities = new ArrayList<>(1);

            display.scan(level, pos, dir, up, excludedEntities);
        }
    }

    private Vector3d getUpVectorForFacing(Direction facing) {
        return switch (facing) {
            case NORTH, SOUTH, EAST, WEST -> new Vector3d(0, 1, 0);
            case UP -> new Vector3d(0, 0, -1);
            case DOWN -> new Vector3d(0, 0, 1);
        };
    }
}
