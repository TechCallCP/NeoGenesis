package shipwrights.genesis.teleportation.impl;

import aeronautics.api.Ship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.joml.primitives.AABBic;
import org.sable.api.SableUtils;

import shipwrights.genesis.NeoGenesisMod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityCollector {

    private final ServerLevel oldLevel;
    private final Map<Entity, Vec3> entityStorage = new HashMap<>();

    public EntityCollector(ServerLevel oldLevel) {
        this.oldLevel = oldLevel;
    }

    public Map<Entity, Vec3> collect(
            List<Ship> ships,
            Vector3dc origin,
            Vector3dc newPos,
            Quaterniondc rotation
    ) {
        ships.forEach(ship -> addEntitiesForShip(ship, origin, newPos, rotation));

        return entityStorage;
    }

    private void addEntitiesForShip(Ship ship, Vector3dc origin, Vector3dc newPos, Quaterniondc rotation) {
        AABBd worldAABB = new AABBd(ship.getWorldAABB());
        AABBic shipAABB = ship.getShipAABB();

        if (shipAABB != null) {
            AABBd shipAABBd = new AABBd(shipAABB.minX(), shipAABB.minY(), shipAABB.minZ(), shipAABB.maxX(), shipAABB.maxY(), shipAABB.maxZ());
            AABB mcBox = new AABB(shipAABBd.minX(), shipAABBd.minY(), shipAABBd.minZ(), shipAABBd.maxX(), shipAABBd.maxY(), shipAABBd.maxZ());

            oldLevel.getEntities(
                    (Entity) null,
                    mcBox.inflate(48),
                    entity -> !entityStorage.containsKey(entity)
            ).forEach(entity -> addEntity(entity, origin, newPos, rotation));

            worldAABB.union(shipAABBd.transform(ship.getPrevTickTransform().getShipToWorld()));
        }

        AABB worldMcBox = new AABB(worldAABB.minX(), worldAABB.minY(), worldAABB.minZ(), worldAABB.maxX(), worldAABB.maxY(), worldAABB.maxZ());

        oldLevel.getEntities(
                (Entity) null,
                worldMcBox.inflate(8 * NeoGenesisMod.getDimensionScale(oldLevel)),
                entity -> !entityStorage.containsKey(entity)
        ).forEach(entity ->
                addEntity(entity, origin, newPos, rotation)
        );
    }

    private void addEntity(Entity entity, Vector3dc origin, Vector3dc newPos, Quaterniondc rotation) {
        Entity root = entity.getRootVehicle();
        if (entityStorage.containsKey(root)) {
            return;
        }
        Vec3 pos = root.position();
        if (!SableUtils.isBlockInShipyard(oldLevel, pos)) {
            Vector3d relPos = new Vector3d(pos.x, pos.y, pos.z).sub(origin);
            rotation.transform(relPos);
            relPos.add(newPos);
            pos = new Vec3(relPos.x, relPos.y, relPos.z);
        }
        entityStorage.put(root, pos);
    }
}