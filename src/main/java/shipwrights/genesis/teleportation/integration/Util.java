package shipwrights.genesis.teleportation.integration;

import aeronautics.api.Ship;
import net.minecraft.server.level.ServerLevel;
import org.joml.primitives.AABBdc;
import org.sable.api.SableUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static List<Ship> getSortedShips(ServerLevel level) {
        String dimension = SableUtils.getDimensionId(level);
        return SableUtils.getShipObjectWorld(level).getLoadedShips().stream()
                .filter(ship -> dimension.equals(ship.getChunkClaimDimension()))
                .sorted(Comparator.<Ship>comparingDouble(s -> {
                            AABBdc box = s.getWorldAABB();
                            return (box.maxX() - box.minX())
                                    * (box.maxY() - box.minY())
                                    * (box.maxZ() - box.minZ());
                        })
                        .reversed()
                        .thenComparingLong(Ship::getId))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}