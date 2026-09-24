package shipwrights.genesis.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;

public class ShipUtils {

    public static boolean isPosInHighShip(ServerLevel level, BlockPos pos) {
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Object container = null;
            try {
                Method getContainer = sableClass.getMethod("getSubLevelContainer", Level.class);
                container = getContainer.invoke(null, level);
            } catch (Exception ignored) {
            }

            if (container != null) {
                Method getAll = container.getClass().getMethod("getAllSubLevels");
                Object subLevelsObj = getAll.invoke(container);
                if (subLevelsObj instanceof Iterable<?> subLevels) {
                    for (Object sl : subLevels) {
                        if (sl != null && checkSubLevelHigh(sl, pos)) return true;
                    }
                }
            } else {
                Method getSubLevels = sableClass.getMethod("getSubLevels", Level.class);
                Object subLevelsObj = getSubLevels.invoke(null, level);
                if (subLevelsObj instanceof Iterable<?> subLevels) {
                    for (Object sl : subLevels) {
                        if (sl != null && checkSubLevelHigh(sl, pos)) return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean checkSubLevelHigh(Object subLevel, BlockPos pos) {
        try {
            Method boxMethod = subLevel.getClass().getMethod("getWorldAABB");
            Object boxObj = boxMethod.invoke(subLevel);
            if (boxObj instanceof AABB aabb && aabb.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                return aabb.maxY > 400.0;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
