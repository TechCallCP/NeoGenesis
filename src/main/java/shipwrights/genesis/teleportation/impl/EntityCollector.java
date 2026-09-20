package shipwrights.genesis.teleportation.impl;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;

import java.lang.reflect.Method;
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
            List<?> ships,
            Vector3dc origin,
            Vector3dc newPos,
            Quaterniondc rotation
    ) {
        ships.forEach(ship -> addEntitiesForShip(ship, origin, newPos, rotation));

        return entityStorage;
    }

    private void addEntitiesForShip(Object ship, Vector3dc origin, Vector3dc newPos, Quaterniondc rotation) {
        AABB worldAABB = getSubLevelAABB(ship);
        AABB shipAABB = getSubLevelShipAABB(ship);

        if (shipAABB != null) {
            oldLevel.getEntities(
                    (Entity) null,
                    shipAABB.inflate(48),
                    entity -> !entityStorage.containsKey(entity)
            ).forEach(entity -> addEntity(entity, origin, newPos, rotation));
        }

        if (worldAABB != null) {
            oldLevel.getEntities(
                    (Entity) null,
                    worldAABB.inflate(8 * NeoGenesisMod.getDimensionScale(oldLevel)),
                    entity -> !entityStorage.containsKey(entity)
            ).forEach(entity ->
                    addEntity(entity, origin, newPos, rotation)
            );
        }
    }

    private void addEntity(Entity entity, Vector3dc origin, Vector3dc newPos, Quaterniondc rotation) {
        Entity root = entity.getRootVehicle();
        if (entityStorage.containsKey(root)) {
            return;
        }
        Vec3 pos = root.position();
        if (!isBlockInShipyard(oldLevel, pos)) {
            Vector3d relPos = new Vector3d(pos.x, pos.y, pos.z).sub(origin);
            rotation.transform(relPos);
            relPos.add(newPos);
            pos = new Vec3(relPos.x, relPos.y, relPos.z);
        }
        entityStorage.put(root, pos);
    }

    private static boolean isBlockInShipyard(ServerLevel level, Vec3 pos) {
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Method method = sableClass.getMethod("isBlockInShipyard", ServerLevel.class, Vec3.class);
            Object res = method.invoke(null, level, pos);
            if (res instanceof Boolean b) return b;
        } catch (Exception ignored) {
        }
        return false;
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

    private static AABB getSubLevelShipAABB(Object subLevel) {
        if (subLevel == null) return null;
        try {
            Method boxMethod = subLevel.getClass().getMethod("getShipAABB");
            Object boxObj = boxMethod.invoke(subLevel);
            if (boxObj instanceof AABB aabb) return aabb;
        } catch (Exception ignored) {
        }
        return null;
    }
}