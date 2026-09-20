package shipwrights.genesis.teleportation.impl;

import aeronautics.api.Ship;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3dc;
import org.sable.api.SableUtils;
import org.sable.api.ShipObjectWorld;
import org.sable.api.ShipTeleportData;
import org.sable.api.ShipTransform;
import org.sable.api.ShipTransformProvider;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.teleportation.TeleportData;

public class ShipTeleporter {

    public static void teleportShip(
            long id,
            TeleportData data,
            ServerLevel newLevel,
            ShipObjectWorld shipWorld
    ) {
        Ship ship = shipWorld.getLoadedShips().getById(id);
        if (ship == null) {
            return;
        }
        ship.setStatic(false);

        ShipTeleportData teleportData = createTeleportData(data, newLevel);
        shipWorld.teleportShip(ship, teleportData);

        applyPostTeleportVelocity(shipWorld, id, ship, data);
    }

    private static ShipTeleportData createTeleportData(
            TeleportData data,
            ServerLevel level
    ) {
        String vsDimName = SableUtils.getDimensionId(level);

        return new ShipTeleportData(
                data.newPos(),
                data.rotation(),
                data.velocity(),
                data.omega(),
                vsDimName,
                NeoGenesisMod.getDimensionScale(level),
                null
        );
    }

    private static void applyPostTeleportVelocity(
            ShipObjectWorld shipWorld,
            long id,
            Ship ship,
            TeleportData data
    ) {
        Vector3dc velocity = data.velocity();
        Vector3dc omega = data.omega();

        if (velocity.lengthSquared() == 0 && omega.lengthSquared() == 0) {
            return;
        }

        ship.setTransformProvider(createVelocityTransformProvider(shipWorld, id, velocity, omega));
    }

    private static ShipTransformProvider createVelocityTransformProvider(
            ShipObjectWorld shipWorld,
            long id,
            Vector3dc velocity,
            Vector3dc omega
    ) {
        return (prevTransform, transform) -> {

            Ship ship = shipWorld.getLoadedShips().getById(id);
            if (ship == null) {
                return null;
            }

            if (!hasTransformChanged(prevTransform, transform)) {
                if (ship.getVelocity().lengthSquared() == 0 &&
                        ship.getAngularVelocity().lengthSquared() == 0) {

                    return new ShipTransformProvider.NextTransformAndVelocityData(
                            transform,
                            velocity,
                            omega
                    );
                }
            } else {
                ship.setTransformProvider(null);
            }

            return null;
        };
    }

    private static boolean hasTransformChanged(
            ShipTransform prevTransform,
            ShipTransform transform
    ) {
        return !prevTransform.getPositionInWorld().equals(transform.getPositionInWorld())
                || !prevTransform.getShipToWorldRotation().equals(transform.getShipToWorldRotation());
    }
}