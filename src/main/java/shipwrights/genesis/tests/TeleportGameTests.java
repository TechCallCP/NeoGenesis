package shipwrights.genesis.tests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.gametest.GameTestHolder;

import org.joml.Vector3d;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.config.GenesisCommonConfig;
import shipwrights.genesis.math.OBB;
import shipwrights.genesis.space.Celestial;

import java.lang.reflect.Method;

/**
 * NeoForge GameTests for the Genesis sub-level teleportation system.
 *
 * <p>Tests assert <em>only</em> on observable outcomes — specifically which dimension a Sable sub-level or
 * entity ends up in. No internal state is directly relied upon, so tests remain valid across engine revisions.
 */
@GameTestHolder(NeoGenesisMod.MOD_ID)
public class TeleportGameTests {

    private static final ResourceKey<Level> SPACE_DIM_KEY =
            ResourceKey.create(Registries.DIMENSION, NeoGenesisMod.SPACE_DIM);

    private static final BlockPos SHIP_ASSEMBLY_REL_POS = new BlockPos(2, 0, 2);

    // -----------------------------------------------------------------------
    // Scaffold
    // -----------------------------------------------------------------------

    /**
     * Smoke test: the GameTest framework is configured correctly and can discover tests in the
     * {@code genesis} namespace.
     */
    @GameTest
    public static void emptyPlatform(GameTestHelper helper) {
        helper.succeed();
    }

    // -----------------------------------------------------------------------
    // Atmosphere Exit
    // -----------------------------------------------------------------------

    /**
     * Verifies that a sub-level positioned above {@code GenesisCommonConfig.getAtmosphereExitHeight()} in a
     * planet dimension is moved to the space dimension within 100 ticks.
     */
    @GameTest(timeoutTicks = 100)
    public static void atmosphereExit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        Object subLevel = TestShipHelper.assembleShip(helper, SHIP_ASSEMBLY_REL_POS);

        ServerLevel spaceLevel = level.getServer().getLevel(SPACE_DIM_KEY);
        if (spaceLevel == null) {
            helper.fail("Space dimension '" + NeoGenesisMod.SPACE_DIM + "' not loaded");
            return;
        }

        TestShipHelper.moveShipAboveAtmosphere(level, subLevel);

        NeoGenesisMod.LOGGER.info("[atmosphereExit] SubLevel moved above atmosphere ({}); polling for teleport to space",
                GenesisCommonConfig.getAtmosphereExitHeight() + 20);

        helper.succeedWhen(() -> {
            boolean inSpace = isSubLevelInLevel(spaceLevel, subLevel);
            if (!inSpace) {
                throw new GameTestAssertException("SubLevel not found in space dimension yet.");
            }

            AABB subLevelAABB = getSubLevelAABB(subLevel);
            if (subLevelAABB != null) {
                Celestial celestial = NeoGenesisMod.getCelestialForLevel(level);
                if (celestial != null) {
                    long ticks = NeoGenesisMod.getTicks(spaceLevel);
                    Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(spaceLevel);
                    OBB shipOBB = OBB.fromAABB(subLevelAABB);
                    if (shipOBB.overlapsWith(celestial.getOBB(ticks, registry))) {
                        throw new GameTestAssertException("SubLevel OBB overlaps celestial OBB after atmosphere exit");
                    }
                }
            }
        });
    }

    // -----------------------------------------------------------------------
    // Planet Entry
    // -----------------------------------------------------------------------

    /**
     * Verifies that a sub-level in the space dimension positioned within the collision radius of
     * the {@code minecraft:overworld} celestial body is moved to the overworld within 100 ticks.
     */
    @GameTest(timeoutTicks = 100)
    public static void planetEntry(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        Object subLevel = TestShipHelper.assembleShip(helper, SHIP_ASSEMBLY_REL_POS);

        ServerLevel spaceLevel = level.getServer().getLevel(SPACE_DIM_KEY);
        if (spaceLevel == null) {
            helper.fail("Space dimension not loaded");
            return;
        }
        ServerLevel overworldLevel = level.getServer().getLevel(Level.OVERWORLD);
        if (overworldLevel == null) {
            helper.fail("Overworld not loaded");
            return;
        }

        TestShipHelper.moveShipNearPlanet(spaceLevel, subLevel, ResourceLocation.parse("minecraft:overworld"));

        NeoGenesisMod.LOGGER.info("[planetEntry] SubLevel moved to space near overworld; polling for planet entry");

        helper.succeedWhen(() -> {
            boolean inOverworld = isSubLevelInLevel(overworldLevel, subLevel);
            if (!inOverworld) {
                throw new GameTestAssertException("SubLevel not found in overworld dimension yet.");
            }
        });
    }

    // -----------------------------------------------------------------------
    // Entity Collection
    // -----------------------------------------------------------------------

    /**
     * Verifies that a non-player entity (Pig) positioned within collection range of a sub-level that
     * exits the atmosphere is moved to the space dimension together with the sub-level.
     */
    @GameTest(timeoutTicks = 100)
    public static void entityTeleportsWithShip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        Object subLevel = TestShipHelper.assembleShip(helper, SHIP_ASSEMBLY_REL_POS);

        ServerLevel spaceLevel = level.getServer().getLevel(SPACE_DIM_KEY);
        if (spaceLevel == null) {
            helper.fail("Space dimension not loaded");
            return;
        }

        Pig pig = EntityType.PIG.create(level);
        if (pig == null) {
            helper.fail("Could not create Pig entity");
            return;
        }

        Vector3d shipPos = getSubLevelCenter(subLevel, helper.absolutePos(SHIP_ASSEMBLY_REL_POS));
        pig.moveTo(shipPos.x() + 2.0D, shipPos.y() + 1.0D, shipPos.z(), 0.0F, 0.0F);
        level.addFreshEntity(pig);

        TestShipHelper.moveShipAboveAtmosphere(level, subLevel);

        NeoGenesisMod.LOGGER.info("[entityTeleportsWithShip] SubLevel and pig above atmosphere; polling for entity teleport");

        helper.succeedWhen(() -> {
            if (!Entity.RemovalReason.CHANGED_DIMENSION.equals(pig.getRemovalReason())) {
                throw new GameTestAssertException("Pig not yet moved to another dimension; removalReason=" + pig.getRemovalReason());
            }
        });
    }

    private static boolean isSubLevelInLevel(ServerLevel level, Object targetSubLevel) {
        if (targetSubLevel == null || level == null) return false;
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Method getSubLevelsMethod = sableClass.getMethod("getSubLevels", Level.class);
            Object subLevelsObj = getSubLevelsMethod.invoke(null, level);
            if (subLevelsObj instanceof Iterable<?> subLevels) {
                for (Object sl : subLevels) {
                    if (sl != null && isSameSubLevel(sl, targetSubLevel)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean isSameSubLevel(Object sl1, Object sl2) {
        if (sl1 == sl2) return true;
        try {
            Method id1 = sl1.getClass().getMethod("getId");
            Method id2 = sl2.getClass().getMethod("getId");
            return id1.invoke(sl1).equals(id2.invoke(sl2));
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

    private static Vector3d getSubLevelCenter(Object subLevel, BlockPos fallbackPos) {
        AABB box = getSubLevelAABB(subLevel);
        if (box != null) {
            return new Vector3d(box.getCenter().x, box.getCenter().y, box.getCenter().z);
        }
        return new Vector3d(fallbackPos.getX() + 0.5, fallbackPos.getY() + 0.5, fallbackPos.getZ() + 0.5);
    }
}