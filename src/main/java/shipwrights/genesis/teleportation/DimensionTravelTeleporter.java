package shipwrights.genesis.teleportation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.teleportation.impl.EntityCollector;
import shipwrights.genesis.teleportation.impl.EntityTeleporter;
import shipwrights.genesis.teleportation.impl.ShipCollector;
import shipwrights.genesis.teleportation.impl.ShipTeleporter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DimensionTravelTeleporter {

    private static final ConcurrentHashMap<Long, Long> shipCooldownExpiry = new ConcurrentHashMap<>();

    public static void teleportShip(
            Object ship,
            TravelDirection direction,
            ServerLevel oldLevel,
            ServerLevel newLevel,
            Vector3dc newPosition,
            Quaterniondc newRotation
    ) {
        if (ship == null) return;
        long gameTime = oldLevel.getGameTime();
        long shipId = getSubLevelId(ship);

        if (shouldSkip(shipId, gameTime)) return;

        Vector3dc origin = getSubLevelPos(ship);
        Object shipWorld = getSableShipWorld(oldLevel);

        ShipCollector collector = new ShipCollector(direction, shipWorld);
        Map<Long, TeleportData> ships = collector.collectConnected(shipId, origin, newPosition, newRotation);

        for (Long shipID : ships.keySet()) {
            if (shouldSkip(shipID, gameTime)) return;
        }

        for (Long shipID : ships.keySet()) {
            markShip(shipID, gameTime);
        }

        EntityCollector entityCollector = new EntityCollector(oldLevel);
        Map<Entity, Vec3> entities = entityCollector.collect(collector.getCollectedShips(), origin, newPosition, newRotation);

        for (var entry : ships.entrySet()) {
            ShipTeleporter.teleportShip(entry.getKey(), entry.getValue(), newLevel, shipWorld);
        }

        for (var entry : entities.entrySet()) {
            EntityTeleporter.teleportEntityAndPassengers(entry.getKey(), newLevel, entry.getValue(), newRotation);
        }
    }

    private static boolean shouldSkip(long shipId, long currentTick) {
        return currentTick < shipCooldownExpiry.getOrDefault(shipId, 0L);
    }

    private static void markShip(long shipId, long currentTick) {
        shipCooldownExpiry.put(shipId, currentTick + 20);
    }

    private static long getSubLevelId(Object subLevel) {
        if (subLevel == null) return 0L;
        try {
            Method idMethod = subLevel.getClass().getMethod("getId");
            Object val = idMethod.invoke(subLevel);
            if (val instanceof Number num) return num.longValue();
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private static Vector3dc getSubLevelPos(Object subLevel) {
        if (subLevel == null) return new Vector3d();
        try {
            Method getTransform = subLevel.getClass().getMethod("getTransform");
            Object transform = getTransform.invoke(subLevel);
            if (transform != null) {
                Method getPos = transform.getClass().getMethod("getPositionInWorld");
                Object posObj = getPos.invoke(transform);
                if (posObj instanceof Vector3dc v) return v;
            }
        } catch (Exception e) {
            try {
                Method boxMethod = subLevel.getClass().getMethod("getWorldAABB");
                Object boxObj = boxMethod.invoke(subLevel);
                if (boxObj instanceof AABB aabb) {
                    return new Vector3d(aabb.getCenter().x, aabb.getCenter().y, aabb.getCenter().z);
                }
            } catch (Exception ignored) {
            }
        }
        return new Vector3d();
    }

    private static Object getSableShipWorld(ServerLevel level) {
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Method method = sableClass.getMethod("getShipObjectWorld", net.minecraft.world.level.Level.class);
            return method.invoke(null, level);
        } catch (Exception ignored) {
        }
        return null;
    }
}