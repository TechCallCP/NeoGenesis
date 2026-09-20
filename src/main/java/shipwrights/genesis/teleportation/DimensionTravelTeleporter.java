package shipwrights.genesis.teleportation;

import aeronautics.api.Ship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;
import org.sable.api.SableUtils;

import shipwrights.genesis.teleportation.impl.EntityCollector;
import shipwrights.genesis.teleportation.impl.EntityTeleporter;
import shipwrights.genesis.teleportation.impl.ShipCollector;
import shipwrights.genesis.teleportation.impl.ShipTeleporter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DimensionTravelTeleporter {

    private static final ConcurrentHashMap<Long, Long> shipCooldownExpiry = new ConcurrentHashMap<>();

    public static void teleportShip(
            Ship ship,
            TravelDirection direction,
            ServerLevel oldLevel,
            ServerLevel newLevel,
            Vector3dc newPosition,
            Quaterniondc newRotation
    ) {
        long gameTime = oldLevel.getGameTime();
        if (shouldSkip(ship.getId(), gameTime)) return;

        Vector3dc origin = ship.getTransform().getPositionInWorld();
        var shipWorld = SableUtils.getShipObjectWorld(oldLevel);

        ShipCollector collector = new ShipCollector(direction, shipWorld);
        Map<Long, TeleportData> ships = collector.collectConnected(ship.getId(), origin, newPosition, newRotation);

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
}