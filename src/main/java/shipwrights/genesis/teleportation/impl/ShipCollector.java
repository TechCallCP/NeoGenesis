package shipwrights.genesis.teleportation.impl;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.teleportation.TeleportData;
import shipwrights.genesis.teleportation.TravelDirection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class ShipCollector {

	private static final double SHIP_COLLECT_RANGE = 10.0;

	private final TravelDirection direction;
	private final Object shipWorld;

	private final Long2ObjectOpenHashMap<TeleportData> ships = new Long2ObjectOpenHashMap<>();
	private final List<Object> collectedShips = new ArrayList<>();
	private double greatestOffset;

	public ShipCollector(
			TravelDirection direction,
			Object shipWorld
	) {
		this.direction = direction;
		this.shipWorld = shipWorld;
	}

	public Map<Long, TeleportData> collectConnected(
			long rootShipId,
			Vector3dc origin,
			Vector3dc newPos,
			Quaterniondc rotation
	) {
		return collectConnected(rootShipId, origin, newPos, rotation, null, null);
	}

	public Map<Long, TeleportData> collectConnected(
			long rootShipId,
			Vector3dc origin,
			Vector3dc newPos,
			Quaterniondc rotation,
			Vector3dc velocity,
			Vector3dc omega
	) {
		this.greatestOffset = 0;
		this.collectConnected(rootShipId, origin, newPos, rotation, velocity, omega, this.collectedShips);
		this.collectNearbyShips(this.collectedShips, origin, newPos, rotation);
		this.finish(this.collectedShips, rotation);
		return this.ships;
	}

	public List<Object> getCollectedShips() {
		return this.collectedShips;
	}

	private void collectConnected(
			long shipId,
			Vector3dc origin,
			Vector3dc newPos,
			Quaterniondc rotation,
			Vector3dc velocity,
			Vector3dc omega,
			List<Object> collected
	) {
		if (this.ships.containsKey(shipId)) {
			return;
		}
		Object subLevel = this.getShip(shipId);
		if (subLevel == null) {
			return;
		}

		Vector3dc pos = getSubLevelPos(subLevel);
		if (velocity == null) {
			velocity = getSubLevelVelocity(subLevel);
		}
		if (omega == null) {
			omega = getSubLevelOmega(subLevel);
		}
		collected.add(subLevel);

		Vector3d relPos = pos.sub(origin, new Vector3d());
		Quaterniond newRotation = new Quaterniond(getSubLevelRotation(subLevel));

		if (this.direction == TravelDirection.PLANET_TO_SPACE) {
			double offset = relPos.y;
			if (offset < this.greatestOffset) {
				this.greatestOffset = offset;
			}
		}

		rotation.transform(relPos);
		velocity = rotation.transform(velocity, new Vector3d());
		newRotation.premul(rotation).normalize();

		if (this.direction == TravelDirection.SPACE_TO_PLANET) {
			double offset = relPos.y;
			if (offset > this.greatestOffset) {
				this.greatestOffset = offset;
			}
		}

		relPos.add(newPos);
		Vector3d velocity0 = new Vector3d(velocity);
		Vector3d omega0 = new Vector3d(omega);

		if (this.direction == TravelDirection.PLANET_TO_SPACE) {
			velocity0.mul(0.0625);
		} else {
			velocity0.mul(2.0);
		}

		this.ships.put(
				shipId,
				new TeleportData(
						relPos,
						newRotation,
						velocity0,
						omega0
				)
		);
	}

	private void collectNearbyShips(List<Object> collected, Vector3dc origin, Vector3dc newPos, Quaterniondc rotation) {
		List<Object> allLoaded = getAllLoadedShips();
		for (int i = 0; i < collected.size(); i++) {
			AABB shipBox = getSubLevelAABB(collected.get(i));
			if (shipBox == null) continue;

			AABB expandedBox = shipBox.inflate(SHIP_COLLECT_RANGE);
			for (Object candidate : allLoaded) {
				long candidateId = getSubLevelId(candidate);
				if (this.ships.containsKey(candidateId)) continue;

				AABB candidateBox = getSubLevelAABB(candidate);
				if (candidateBox != null && expandedBox.intersects(candidateBox)) {
					this.collectConnected(candidateId, origin, newPos, rotation, null, null, collected);
				}
			}
		}
	}

	private void finish(List<Object> collected, Quaterniondc rotation) {
		Vector3d offset = new Vector3d(0, -this.greatestOffset, 0);
		if (this.direction == TravelDirection.PLANET_TO_SPACE) {
			rotation.transform(offset);
		}
		for (Object ship : collected) {
			long id = getSubLevelId(ship);
			TeleportData tData = this.ships.get(id);
			if (tData != null) {
				tData.newPos().add(offset);
			}
		}
	}

	private Object getShip(long shipId) {
		for (Object ship : getAllLoadedShips()) {
			if (getSubLevelId(ship) == shipId) {
				return ship;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private List<Object> getAllLoadedShips() {
		List<Object> list = new ArrayList<>();
		if (this.shipWorld != null) {
			try {
				Method getLoadedMethod = this.shipWorld.getClass().getMethod("getLoadedShips");
				Object res = getLoadedMethod.invoke(this.shipWorld);
				if (res instanceof Iterable<?> iterable) {
					for (Object o : iterable) {
						if (o != null) list.add(o);
					}
				}
			} catch (Exception ignored) {
			}
		}
		return list;
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
		AABB box = getSubLevelAABB(subLevel);
		if (box != null) {
			return new Vector3d(box.getCenter().x, box.getCenter().y, box.getCenter().z);
		}
		return new Vector3d();
	}

	private static Vector3dc getSubLevelVelocity(Object subLevel) {
		if (subLevel != null) {
			try {
				Method velMethod = subLevel.getClass().getMethod("getVelocity");
				Object val = velMethod.invoke(subLevel);
				if (val instanceof Vector3dc v) return v;
			} catch (Exception ignored) {
			}
		}
		return new Vector3d();
	}

	private static Vector3dc getSubLevelOmega(Object subLevel) {
		if (subLevel != null) {
			try {
				Method omegaMethod = subLevel.getClass().getMethod("getAngularVelocity");
				Object val = omegaMethod.invoke(subLevel);
				if (val instanceof Vector3dc v) return v;
			} catch (Exception ignored) {
			}
		}
		return new Vector3d();
	}

	private static Quaterniondc getSubLevelRotation(Object subLevel) {
		if (subLevel != null) {
			try {
				Method rotMethod = subLevel.getClass().getMethod("getRotation");
				Object val = rotMethod.invoke(subLevel);
				if (val instanceof Quaterniondc q) return q;
			} catch (Exception ignored) {
			}
		}
		return new Quaterniond();
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