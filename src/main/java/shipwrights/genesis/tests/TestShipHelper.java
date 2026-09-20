package shipwrights.genesis.tests;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.config.GenesisCommonConfig;
import shipwrights.genesis.space.Celestial;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Utility for creating and positioning Sable sub-level ships in GameTests.
 *
 * <p>Sub-level assembly interacts directly with Sable API endpoints (or falls back to commands),
 * making the returned sub-level instance immediately available for verification.
 */
public class TestShipHelper {

    private static final int MAX_ASSEMBLY_BLOCKS = 256;

    /**
     * Places a 3×3 stone platform at {@code relPos} (relative to the GameTest structure origin),
     * flood-fills the connected blocks, and assembles them into a Sable sub-level ship synchronously.
     *
     * <p>The returned sub-level object is immediately usable.
     */
    public static Object assembleShip(GameTestHelper helper, BlockPos relPos) {
        ServerLevel level = helper.getLevel();
        BlockPos absPos = helper.absolutePos(relPos);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(absPos.offset(dx, 0, dz), Blocks.STONE.defaultBlockState(), 3);
            }
        }

        List<BlockPos> blocks = collectBlocks(level, absPos);
        NeoGenesisMod.LOGGER.info("[TestShipHelper] assembleShip: collecting {} blocks at {}", blocks.size(), absPos);

        Object subLevel = null;
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            try {
                Method createMethod = sableClass.getMethod("createSubLevel", ServerLevel.class, List.class);
                subLevel = createMethod.invoke(null, level, blocks);
            } catch (Exception e1) {
                try {
                    Method assembleMethod = sableClass.getMethod("assemble", ServerLevel.class, List.class);
                    subLevel = assembleMethod.invoke(null, level, blocks);
                } catch (Exception ignored) {
                }
            }
        } catch (Throwable ignored) {
        }

        if (subLevel == null) {
            String cmd = String.format("sable assemble %d %d %d", absPos.getX(), absPos.getY(), absPos.getZ());
            CommandSourceStack cmdSource = level.getServer().createCommandSourceStack();
            level.getServer().getCommands().performPrefixedCommand(cmdSource, cmd);
        }

        String subLevelId = getSubLevelIdentifier(subLevel);
        NeoGenesisMod.LOGGER.info("[TestShipHelper] assembleShip: created Sable sub-level id='{}'", subLevelId);
        return subLevel;
    }

    /**
     * Teleports {@code subLevel} to Y = {@code atmosphereExitHeight + 20} within the same dimension
     * so that {@code PlanetToSpaceTeleporter} will pick it up on the next tick.
     */
    public static void moveShipAboveAtmosphere(ServerLevel level, Object subLevel) {
        Vector3d pos = getSubLevelPosition(subLevel, level, BlockPos.ZERO);
        double targetY = GenesisCommonConfig.getAtmosphereExitHeight() + 20.0;
        String dimId = level.dimension().location().toString();
        String subLevelId = getSubLevelIdentifier(subLevel);

        String cmd = String.format("execute in %s run sable teleport %s %f %f %f",
                dimId, subLevelId, pos.x(), targetY, pos.z());
        NeoGenesisMod.LOGGER.info("[TestShipHelper] moveShipAboveAtmosphere: {}", cmd);
        CommandSourceStack cmdSource = level.getServer().createCommandSourceStack();
        level.getServer().getCommands().performPrefixedCommand(cmdSource, cmd);
    }

    /**
     * Teleports {@code subLevel} into the space dimension, positioned within the collision radius of
     * the celestial identified by {@code celestialId}.
     */
    public static void moveShipNearPlanet(ServerLevel spaceLevel, Object subLevel, ResourceLocation celestialId) {
        Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(spaceLevel);
        if (registry == null) {
            NeoGenesisMod.LOGGER.warn("[TestShipHelper] Celestial registry unavailable; cannot position ship");
            return;
        }

        Celestial celestial = registry.get(celestialId);
        if (celestial == null) {
            NeoGenesisMod.LOGGER.warn("[TestShipHelper] Celestial '{}' not found in registry; cannot position ship for planet entry test", celestialId);
            return;
        }

        long ticks = NeoGenesisMod.getTicks(spaceLevel);
        Vector3dc celestialPos = celestial.getPosition(ticks, registry);
        double targetDist = celestial.getActualSize() * 0.5;
        Vec3 targetPos = new Vec3(
                celestialPos.x(),
                celestialPos.y() + targetDist,
                celestialPos.z()
        );

        String spaceDimId = spaceLevel.dimension().location().toString();
        String subLevelId = getSubLevelIdentifier(subLevel);
        String cmd = String.format("execute in %s run sable teleport %s %f %f %f",
                spaceDimId, subLevelId, targetPos.x, targetPos.y, targetPos.z);

        NeoGenesisMod.LOGGER.info("[TestShipHelper] moveShipNearPlanet: celestial='{}' pos=({},{},{}) cmd={}",
                celestialId, celestialPos.x(), celestialPos.y(), celestialPos.z(), cmd);
        CommandSourceStack cmdSource = spaceLevel.getServer().createCommandSourceStack();
        spaceLevel.getServer().getCommands().performPrefixedCommand(cmdSource, cmd);
    }

    /** Flood-fills from {@code origin} collecting all 6-connected non-air blocks up to {@value MAX_ASSEMBLY_BLOCKS}. */
    private static List<BlockPos> collectBlocks(ServerLevel level, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        if (!level.getBlockState(origin).isAir()) {
            visited.add(origin);
            queue.add(origin);
        }

        while (!queue.isEmpty() && result.size() < MAX_ASSEMBLY_BLOCKS) {
            BlockPos pos = queue.poll();
            result.add(pos);
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.add(neighbor) && !level.getBlockState(neighbor).isAir()) {
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }

    private static Vector3d getSubLevelPosition(Object subLevel, ServerLevel level, BlockPos fallbackPos) {
        if (subLevel != null) {
            try {
                Method boxMethod = subLevel.getClass().getMethod("getWorldAABB");
                Object boxObj = boxMethod.invoke(subLevel);
                if (boxObj instanceof AABB aabb) {
                    return new Vector3d(aabb.getCenter().x, aabb.getCenter().y, aabb.getCenter().z);
                }
            } catch (Exception e) {
                try {
                    Method boxMethod = subLevel.getClass().getMethod("getBoundingBox");
                    Object boxObj = boxMethod.invoke(subLevel);
                    if (boxObj instanceof AABB aabb) {
                        return new Vector3d(aabb.getCenter().x, aabb.getCenter().y, aabb.getCenter().z);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return new Vector3d(fallbackPos.getX() + 0.5, fallbackPos.getY() + 0.5, fallbackPos.getZ() + 0.5);
    }

    private static String getSubLevelIdentifier(Object subLevel) {
        if (subLevel != null) {
            try {
                Method idMethod = subLevel.getClass().getMethod("getId");
                return String.valueOf(idMethod.invoke(subLevel));
            } catch (Exception e) {
                try {
                    Method slugMethod = subLevel.getClass().getMethod("getSlug");
                    return String.valueOf(slugMethod.invoke(subLevel));
                } catch (Exception ignored) {
                }
            }
        }
        return "0";
    }
}
