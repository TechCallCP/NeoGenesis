package shipwrights.genesis.teleportation.integration;

import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.config.GenesisCommonConfig;
import shipwrights.genesis.math.OBB;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.teleportation.DimensionTravelTeleporter;
import shipwrights.genesis.teleportation.TravelDirection;

import java.lang.reflect.Method;
import java.util.*;

import static shipwrights.genesis.teleportation.integration.Util.getSortedShips;

public class SpaceToPlanetTeleporter {
	private static final int LANDING_ACCURACY = 8; // Randomization range in chunks

	private final boolean gameTest;

	public SpaceToPlanetTeleporter(boolean gameTest) {
		this.gameTest = gameTest;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLevelTick(LevelTickEvent.Post event) {
		if (event.getLevel() instanceof ServerLevel serverLevel && NeoGenesisMod.isSpaceDimension(serverLevel)) {
			if (gameTest || !serverLevel.getPlayers(u -> true, 1).isEmpty()) {
				tick(serverLevel);
			}
		}
	}

	private static void tick(ServerLevel level) {
		long ticks = NeoGenesisMod.getTicks(level);
		Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(level);

		for (Object ship : getSortedShips(level)) {
			AABB worldAABB = getSubLevelAABB(ship);
			Vec3 shipCenter = worldAABB != null ? worldAABB.getCenter() : Vec3.ZERO;
			AABB shipAABB = getSubLevelShipAABB(ship);

			Celestial nearest = getNearestPlanet(ship, ticks, registry);
			if (isSubLevelStatic(ship) || nearest == null || shipAABB == null) continue;

			if (!shipOverlapsCelestial(ship, shipAABB, nearest, ticks, registry)) continue;

			PlayerTeam team = level.getScoreboard().getPlayerTeam(registry.getResourceKey(nearest).orElseThrow().location().getPath());
			if (team != null) {
				Collection<String> teamShips = team.getPlayers();
				String slug = getSubLevelSlug(ship);
				if (!teamShips.contains(slug)) {
					NeoForge.EVENT_BUS.post(new TeleportDisallowedEvent(ship, nearest));
					continue;
				}
			}

			ServerLevel targetLevel = getTargetLevel(level, nearest, registry);
			if (targetLevel == null) continue;

			Vector3d newPos = computePlanetTarget(level);
			Quaterniond rotation = getNewShipRot(shipCenter, nearest, ticks, registry);

			DimensionTravelTeleporter.teleportShip(ship, TravelDirection.SPACE_TO_PLANET, level, targetLevel, newPos, rotation);
		}
	}

	private static boolean shipOverlapsCelestial(Object ship, AABB shipAABB, Celestial nearest, long ticks, Registry<Celestial> registry) {
		Matrix4dc transform = getSubLevelTransformMatrix(ship);
		if (transform == null) return false;
		return OBB.fromShip(shipAABB, transform).overlapsWith(nearest.getOBB(ticks, registry));
	}

	private static @Nullable ServerLevel getTargetLevel(ServerLevel level, Celestial nearest, Registry<Celestial> registry) {
		ResourceKey<Level> targetDimension = ResourceKey.create(
				Registries.DIMENSION,
				registry.getResourceKey(nearest).orElseThrow().location()
		);
		return level.getServer().getLevel(targetDimension);
	}

	private static Vector3d computePlanetTarget(ServerLevel level) {
		ChunkPos landingChunkPos = new ChunkPos(
				level.random.nextInt(LANDING_ACCURACY * 2 + 1) - LANDING_ACCURACY,
				level.random.nextInt(LANDING_ACCURACY * 2 + 1) - LANDING_ACCURACY
		);
		return new Vector3d(
				SectionPos.sectionToBlockCoord(landingChunkPos.x),
				GenesisCommonConfig.getAtmosphereEntryHeight(),
				SectionPos.sectionToBlockCoord(landingChunkPos.z)
		);
	}

	private static Quaterniond getNewShipRot(Vec3 shipCenter, Celestial nearest, long ticks, Registry<Celestial> registry) {
		Vector3dc planetPos = nearest.getPosition(ticks, registry);
		Vector3d directionToPlanet = new Vector3d(
				shipCenter.x - planetPos.x(),
				shipCenter.y - planetPos.y(),
				shipCenter.z - planetPos.z()
		).normalize();
		Quaterniond rotation = new Quaterniond().rotateTo(new Vector3d(0, 1, 0), directionToPlanet);
		nearest.getRotation(ticks, 0f, registry).mul(rotation, rotation).conjugate();
		return rotation;
	}

	@Nullable static Celestial getNearestPlanet(Object ship, long ticks, Registry<Celestial> registry) {
		AABB shipAABB = getSubLevelShipAABB(ship);
		if (shipAABB != null) {
			Matrix4dc transform = getSubLevelTransformMatrix(ship);
			if (transform == null) return null;
			OBB shipOBB = OBB.fromShip(shipAABB, transform);

			Optional<Celestial> nearest = registry.stream()
					.filter(c -> c.type().isVisitable())
					.map(c -> Map.entry(c, c.getOBB(ticks, registry).distanceTo(shipOBB)))
					.min(Comparator.comparingDouble(Map.Entry::getValue))
					.map(Map.Entry::getKey);
			if (nearest.isPresent()) {
				return nearest.get();
			}
		}
		return null;
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

	private static String getSubLevelSlug(Object subLevel) {
		if (subLevel == null) return "";
		try {
			Method slugMethod = subLevel.getClass().getMethod("getSlug");
			Object res = slugMethod.invoke(subLevel);
			if (res instanceof String s) return s;
		} catch (Exception ignored) {
		}
		return "";
	}

	private static Matrix4dc getSubLevelTransformMatrix(Object subLevel) {
		if (subLevel == null) return null;
		try {
			Method getShipToWorld = subLevel.getClass().getMethod("getShipToWorld");
			Object matrixObj = getShipToWorld.invoke(subLevel);
			if (matrixObj instanceof Matrix4dc matrix) return matrix;
		} catch (Exception e) {
			try {
				Method getTransform = subLevel.getClass().getMethod("getTransform");
				Object transform = getTransform.invoke(subLevel);
				if (transform != null) {
					Method getMatrix = transform.getClass().getMethod("getShipToWorld");
					Object matrixObj = getMatrix.invoke(transform);
					if (matrixObj instanceof Matrix4dc matrix) return matrix;
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}
}