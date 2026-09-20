package shipwrights.genesis.teleportation.integration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.config.GenesisCommonConfig;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.VantagePoint;
import shipwrights.genesis.teleportation.DimensionTravelTeleporter;
import shipwrights.genesis.teleportation.TravelDirection;

import java.lang.reflect.Method;

import static shipwrights.genesis.teleportation.integration.Util.getSortedShips;

public class PlanetToSpaceTeleporter {
	private final boolean gameTest;

	public PlanetToSpaceTeleporter(boolean gameTest) {
		this.gameTest = gameTest;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel serverLevel) {
			if (gameTest || !serverLevel.getPlayers(u -> true, 1).isEmpty()) {
				tick(serverLevel);
			}
		}
	}

	private static void tick(ServerLevel level) {
		Celestial body = NeoGenesisMod.getCelestialForLevel(level);
		ServerLevel spaceLevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, NeoGenesisMod.SPACE_DIM));

		if (body == null || spaceLevel == null) {
			return;
		}

		long ticks = NeoGenesisMod.getTicks(level);

		for (Object ship : getSortedShips(level)) {
			Vector3dc shipPos = getSubLevelPos(ship);
			if (!isSubLevelStatic(ship) && shipPos.y() > GenesisCommonConfig.getAtmosphereExitHeight()) {

				if (VantagePoint.get(level, shipPos, ticks, 0f) instanceof VantagePoint.OnCelestial vantagePoint) {
					DimensionTravelTeleporter.teleportShip(
							ship,
							TravelDirection.PLANET_TO_SPACE,
							level,
							spaceLevel,
							computeSpaceTarget(vantagePoint),
							vantagePoint.getCelestialRotation().mul(vantagePoint.cameraRotationFromNorthPole().conjugate(new Quaterniond()), new Quaterniond())
					);
				}
			}
		}
	}

	// Package-private — accessed by tests
	static Quaterniondc computeSpaceRotation(Quaterniondc vantageRotation, Quaterniondc shipRotation) {
		return new Quaterniond(vantageRotation).mul(shipRotation, new Quaterniond());
	}

	private static Vector3d computeSpaceTarget(VantagePoint.OnCelestial vantagePoint) {
		Vector3d targetPos = new Vector3d(0, vantagePoint.celestial().getActualSize() + 20, 0);
		vantagePoint.cameraRotationFromNorthPole().conjugate(new Quaterniond()).transform(targetPos);
		vantagePoint.getCelestialRotation().transform(targetPos);
		targetPos.add(vantagePoint.getPosition());
		return targetPos;
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

	private static boolean isSubLevelStatic(Object subLevel) {
		if (subLevel == null) return false;
		try {
			Method staticMethod = subLevel.getClass().getMethod("isStatic");
			Object res = staticMethod.invoke(subLevel);
			if (res instanceof Boolean b) return b;
		} catch (Exception ignored) {
		}
		return false;
	}
}