package shipwrights.genesis.teleportation.integration;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Util {

    /**
     * Returns a list of Sable sub-level objects in the given level, sorted by bounding box volume (descending),
     * then by sub-level ID.
     */
    public static List<Object> getSortedShips(ServerLevel level) {
        List<Object> subLevels = getLoadedSubLevels(level);
        subLevels.sort(
                Comparator.<Object>comparingDouble(Util::getSubLevelVolume)
                        .reversed()
                        .thenComparingLong(Util::getSubLevelId)
        );
        return new ArrayList<>(subLevels);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getLoadedSubLevels(ServerLevel level) {
        List<Object> result = new ArrayList<>();
        if (level == null) return result;

        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            try {
                Method getSubLevelsMethod = sableClass.getMethod("getSubLevels", net.minecraft.world.level.Level.class);
                Object obj = getSubLevelsMethod.invoke(null, level);
                if (obj instanceof Iterable<?> iterable) {
                    for (Object o : iterable) {
                        if (o != null) result.add(o);
                    }
                }
            } catch (Exception e1) {
                try {
                    Method getShipWorldMethod = sableClass.getMethod("getShipObjectWorld", net.minecraft.world.level.Level.class);
                    Object world = getShipWorldMethod.invoke(null, level);
                    if (world != null) {
                        Method getLoadedMethod = world.getClass().getMethod("getLoadedShips");
                        Object obj = getLoadedMethod.invoke(world);
                        if (obj instanceof Iterable<?> iterable) {
                            String dimId = level.dimension().location().toString();
                            for (Object o : iterable) {
                                if (o != null && matchesDimension(o, dimId)) {
                                    result.add(o);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Throwable ignored) {
        }

        return result;
    }

    private static boolean matchesDimension(Object subLevel, String dimensionId) {
        try {
            Method getDimMethod = subLevel.getClass().getMethod("getChunkClaimDimension");
            Object dim = getDimMethod.invoke(subLevel);
            return dimensionId.equals(String.valueOf(dim));
        } catch (Exception e) {
            return true;
        }
    }

    private static double getSubLevelVolume(Object subLevel) {
        AABB box = getSubLevelAABB(subLevel);
        if (box == null) return 0.0;
        return (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
    }

    private static long getSubLevelId(Object subLevel) {
        if (subLevel == null) return 0L;
        try {
            Method idMethod = subLevel.getClass().getMethod("getId");
            Object val = idMethod.invoke(subLevel);
            if (val instanceof Number num) {
                return num.longValue();
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private static AABB getSubLevelAABB(Object subLevel) {
        if (subLevel == null) return null;
        try {
            Method boxMethod = subLevel.getClass().getMethod("getWorldAABB");
            Object boxObj = boxMethod.invoke(subLevel);
            if (boxObj instanceof AABB aabb) return aabb;
        } catch (Exception e) {
            try {
                Method boxMethod = subLevel.getClass().getMethod("getBoundingBox");
                Object boxObj = boxMethod.invoke(subLevel);
                if (boxObj instanceof AABB aabb) return aabb;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}