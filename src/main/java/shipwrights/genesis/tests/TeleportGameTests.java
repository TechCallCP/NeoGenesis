package shipwrights.genesis.tests;

import aeronautics.api.Ship;
import aeronautics.api.ShipAssembler;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.joml.Vector3dc;
import org.sable.api.SableUtils;
import org.sable.block.TestHingeBlock;
import org.sable.blockentity.TestHingeBlockEntity;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.config.GenesisCommonConfig;
import shipwrights.genesis.math.OBB;
import shipwrights.genesis.space.Celestial;

import java.util.UUID;

/**
 * NeoForge GameTests for the Genesis ship teleportation system.
 *
 * <p>Tests assert <em>only</em> on observable outcomes — specifically which dimension a ship or
 * entity ends up in. No internal state (LAUNCHING/LANDING maps, DimensionTravelTeleporter fields,
 * velocity values) is inspected, so tests remain valid across teleportation redesigns.
 *
 * <p>Ship assembly calls {@link ShipAssembler} directly via {@link TestShipHelper#assembleShip},
 * which returns the {@link Ship} synchronously. Tests no longer need a polling phase
 * to discover the assembled ship.
 */
@GameTestHolder(NeoGenesisMod.MOD_ID)
public class TeleportGameTests {

    private static final ResourceKey<net.minecraft.world.level.Level> SPACE_DIM_KEY =
            ResourceKey.create(Registries.DIMENSION, NeoGenesisMod.SPACE_DIM);

    /** Relative position within the test structure where the ship platform is placed. */
    private static final BlockPos SHIP_ASSEMBLY_REL_POS = new BlockPos(Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(2));

    // -----------------------------------------------------------------------
    // § 2 – Scaffold
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
    // § 4 – Atmosphere Exit
    // -----------------------------------------------------------------------

    /**
     * Verifies that a ship positioned above {@code GenesisCommonConfig.getAtmosphereExitHeight()} in a
     * planet dimension is moved to the space dimension within 100 ticks.
     */
    @GameTest(timeoutTicks = 100)
    public static void atmosphereExit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        Ship ship = TestShipHelper.assembleShip(helper, SHIP_ASSEMBLY_REL_POS);

        ServerLevel spaceLevel = level.getServer().getLevel(SPACE_DIM_KEY);
        if (spaceLevel == null) {
            helper.fail("Space dimension '" + NeoGenesisMod.SPACE_DIM + "' not loaded");
            return;
        }
        String expectedSpaceDim = SableUtils.getDimensionId(spaceLevel);

        TestShipHelper.moveShipAboveAtmosphere(level, ship);
        Long shipId = ship.getId();

        NeoGenesisMod.LOGGER.info("[atmosphereExit] Ship id={} moved above atmosphere ({}); polling for teleport to '{}'",
                shipId, GenesisCommonConfig.getAtmosphereExitHeight() + 20, expectedSpaceDim);

        helper.succeedWhen(() -> {
            Ship found = SableUtils.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (found == null) {
                throw new GameTestAssertException("Ship record lost for id=" + shipId);
            }
            NeoGenesisMod.LOGGER.debug("[atmosphereExit] tick check: ship id={} chunkClaimDim='{}'",
                    shipId, found.getChunkClaimDimension());
            if (!expectedSpaceDim.equals(found.getChunkClaimDimension())) {
                throw new GameTestAssertException(
                        "Ship not in space yet; chunkClaimDimension='" + found.getChunkClaimDimension()
                                + "' (expected='" + expectedSpaceDim + "')");
            }
            var shipAABB = found.getShipAABB();
            if (shipAABB != null) {
                Celestial celestial = NeoGenesisMod.getCelestialForLevel(level);
                if (celestial != null) {
                    Long ticks = NeoGenesisMod.getTicks(spaceLevel);
                    Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(spaceLevel);
                    OBB shipOBB = OBB.fromShip(shipAABB, found.getShipToWorld());
                    if (shipOBB.overlapsWith(celestial.getOBB(ticks, registry))) {
                        throw new GameTestAssertException(
                                "Ship OBB overlaps celestial OBB after atmosphere exit (ping-pong risk)");
                    }
                }
            }
        });
    }

    // -----------------------------------------------------------------------
    // § 5 – Planet Entry
    // -----------------------------------------------------------------------

    /**
     * Verifies that a ship in the space dimension positioned within the collision radius of
     * the {@code minecraft:overworld} celestial body is moved to the overworld within 100 ticks.
     */
    @GameTest(timeoutTicks = 100)
    public static void planetEntry(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        Ship ship = TestShipHelper.assembleShip(helper, SHIP_ASSEMBLY_REL_POS);

        ServerLevel spaceLevel = level.getServer().getLevel(SPACE_DIM_KEY);
        if (spaceLevel == null) {
            helper.fail("Space dimension not loaded");
            return;
        }
        ServerLevel overworldLevel = level.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworldLevel == null) {
            helper.fail("Overworld not loaded");
            return;
        }
        String expectedOverworldDim = SableUtils.getDimensionId(overworldLevel);

        // Move ship into space near the overworld celestial's position
        TestShipHelper.moveShipNearPlanet(spaceLevel, ship, ResourceLocation.parse("minecraft:overworld"));
        Long shipId = ship.getId();

        NeoGenesisMod.LOGGER.info("[planetEntry] Ship id={} moved to space near overworld; polling for planet entry to '{}'",
                shipId, expectedOverworldDim);

        helper.succeedWhen(() -> {
            Ship found = SableUtils.getShipObjectWorld(spaceLevel).getAllShips().getById(shipId);
            if (found == null) {
                throw new GameTestAssertException("Ship record lost for id=" + shipId);
            }
            NeoGenesisMod.LOGGER.debug("[planetEntry] tick check: ship id={} chunkClaimDim='{}'",
                    shipId, found.getChunkClaimDimension());
            if (!expectedOverworldDim.equals(found.getChunkClaimDimension())) {
                throw new GameTestAssertException(
                        "Ship not in overworld yet; chunkClaimDimension='" + found.getChunkClaimDimension() + "'");
            }
        });
    }

    // -----------------------------------------------------------------------
    // § 6 – Entity Collection
    // -----------------------------------------------------------------------

    /**
     * Verifies that a non-player entity (Pig) positioned within collection range of a ship that
     * exits the atmosphere is moved to the space dimension together with the ship.
     */
    @GameTest(timeoutTicks = 100)
    public static void entityTeleportsWithShip(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        Ship ship = TestShipHelper.assembleShip(helper, SHIP_ASSEMBLY_REL_POS);

        ServerLevel spaceLevel = level.getServer().getLevel(SPACE_DIM_KEY);
        if (spaceLevel == null) {
            helper.fail("Space dimension not loaded");
            return;
        }

        // Spawn a Pig within entity collection range (≤ 8 blocks from ship AABB)
        Pig pig = EntityType.PIG.create(level);
        if (pig == null) {
            helper.fail("Could not create Pig entity");
            return;
        }
        Vector3dc shipPos = ship.getTransform().getPositionInWorld();
        pig.moveTo(shipPos.x() + 2.0D, shipPos.y() + 1.0D, shipPos.z(), Float.valueOf(0.0F), Float.valueOf(0.0F));
        level.addFreshEntity(pig);

        TestShipHelper.moveShipAboveAtmosphere(level, ship);
        Long shipId = ship.getId();

        NeoGenesisMod.LOGGER.info("[entityTeleportsWithShip] Ship id={} and pig UUID={} above atmosphere; polling for entity teleport",
                shipId, pig.getStringUUID());

        helper.succeedWhen(() -> {
            NeoGenesisMod.LOGGER.debug("[entityTeleportsWithShip] tick check: pig removalReason={}",
                    pig.getRemovalReason());
            if (!net.minecraft.world.entity.Entity.RemovalReason.CHANGED_DIMENSION.equals(pig.getRemovalReason())) {
                throw new GameTestAssertException(
                        "Pig not yet moved to another dimension; removalReason=" + pig.getRemovalReason());
            }
        });
    }

    // -----------------------------------------------------------------------
    // § 7 – Connected Ships
    // -----------------------------------------------------------------------

    /**
     * Verifies that two ships connected by a joint both move to the space dimension when
     * ship A exits the atmosphere.
     */
    @GameTest(timeoutTicks = 200)
    public static void connectedShipsTeleportTogether(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // Assemble ship A from a 3×3 stone platform.
        Ship shipA = TestShipHelper.assembleShip(helper, new BlockPos(Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(1)));
        Long shipAId = shipA.getId();

        Vector3dc shipAInShipPos = shipA.getTransform().getPositionInShip();
        BlockPos hingeWorldPos = new BlockPos(
                Double.valueOf(shipAInShipPos.x() - 0.5D).intValue(),
                Double.valueOf(shipAInShipPos.y() - 0.5D).intValue(),
                Double.valueOf(shipAInShipPos.z() - 0.5D).intValue());
        level.setBlockAndUpdate(hingeWorldPos, SableUtils.TEST_HINGE.defaultBlockState());

        TestHingeBlockEntity hingeBlockEntity =
                (TestHingeBlockEntity) level.getBlockEntity(hingeWorldPos);
        if (hingeBlockEntity == null) {
            helper.fail("TestHingeBlockEntity not created at " + hingeWorldPos);
            return;
        }

        TestHingeBlock.INSTANCE.use(
                SableUtils.TEST_HINGE.defaultBlockState(),
                level,
                hingeWorldPos,
                FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "genesis_test")),
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        new Vec3(hingeWorldPos.getX() + 0.5D, hingeWorldPos.getY() + 0.5D, hingeWorldPos.getZ() + 0.5D),
                        Direction.UP,
                        hingeWorldPos,
                        Boolean.FALSE));

        BlockPos shipBPos = hingeBlockEntity.getOtherHingePos();
        if (shipBPos == null) {
            helper.fail("TestHingeBlock.use() did not set otherHingePos; joint creation may have failed");
            return;
        }
        Ship shipBRef = SableUtils.getShipManagingPos(level, shipBPos);
        if (shipBRef == null) {
            helper.fail("Could not find ship B managing pos=" + shipBPos);
            return;
        }
        Long shipBId = shipBRef.getId();

        ServerLevel spaceLevel = level.getServer().getLevel(SPACE_DIM_KEY);
        if (spaceLevel == null) {
            helper.fail("Space dimension not loaded");
            return;
        }
        String expectedSpaceDim = SableUtils.getDimensionId(spaceLevel);

        helper.runAfterDelay(Integer.valueOf(30), () -> {
            Ship shipALookup = SableUtils.getShipObjectWorld(level).getAllShips().getById(shipAId);
            if (shipALookup == null) {
                helper.fail("Ship A record lost before launch");
                return;
            }
            NeoGenesisMod.LOGGER.info("[connectedShipsTeleportTogether] launching shipA id={} above atmosphere; shipB id={}",
                    shipAId, shipBId);
            TestShipHelper.moveShipAboveAtmosphere(level, shipALookup);
        });

        helper.succeedWhen(() -> {
            Ship foundA = SableUtils.getShipObjectWorld(level).getAllShips().getById(shipAId);
            Ship foundB = SableUtils.getShipObjectWorld(level).getAllShips().getById(shipBId);
            if (foundA == null || foundB == null) {
                throw new GameTestAssertException(
                        "Ship record(s) lost: shipA=" + shipAId + " shipB=" + shipBId);
            }
            NeoGenesisMod.LOGGER.debug("[connectedShipsTeleportTogether] shipA dim='{}' shipB dim='{}'",
                    foundA.getChunkClaimDimension(), foundB.getChunkClaimDimension());
            if (!expectedSpaceDim.equals(foundA.getChunkClaimDimension())) {
                throw new GameTestAssertException("Ship A not in space: " + foundA.getChunkClaimDimension());
            }
            if (!expectedSpaceDim.equals(foundB.getChunkClaimDimension())) {
                throw new GameTestAssertException("Ship B not in space: " + foundB.getChunkClaimDimension());
            }
        });
    }
}