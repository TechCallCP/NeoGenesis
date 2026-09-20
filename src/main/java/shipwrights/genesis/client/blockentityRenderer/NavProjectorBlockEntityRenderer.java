package shipwrights.genesis.client.blockentityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.content.blockentity.NavProjectorBlockEntity;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.VantagePoint;
import shipwrights.genesis.space.type.BuiltinCelestialTypes;

import java.lang.reflect.Method;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class NavProjectorBlockEntityRenderer implements BlockEntityRenderer<NavProjectorBlockEntity> {

    public NavProjectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(NavProjectorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        Level level = blockEntity.getLevel();

        if (level == null) {
            poseStack.popPose();
            return;
        }

        long ticks = NeoGenesisMod.getTicks(level);

        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.scale(0.02F, 0.02F, 0.02F);
        BlockPos pos = blockEntity.getBlockPos();

        Object ship = getSableSubLevel(level, pos);
        boolean isOnShip = ship != null;

        Vector3dc currentPos = null;
        int scale_factor = 1000;

        poseStack.translate(-0.5D, -0.5D, -0.5D);

        blockRenderer.renderSingleBlock(Blocks.WHITE_CONCRETE.defaultBlockState(),
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );

        poseStack.translate(0.5D, 0.5D, 0.5D);

        Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(level);
        VantagePoint vp = VantagePoint.get(level, new Vector3d(), ticks, partialTick);
        Celestial currentPlanet = vp instanceof VantagePoint.OnCelestial oc ? oc.celestial() : null;

        if (currentPlanet != null) {
            poseStack.mulPose(new Quaternionf(currentPlanet.getRotation(ticks, partialTick, registry)).invert());

            if (isOnShip) {
                Quaterniondc rot1 = getSubLevelRotation(ship).invert(new Quaterniond());
                poseStack.mulPose(new Quaternionf(rot1));
            }

            currentPos = currentPlanet.getPosition(ticks, partialTick, registry);

            poseStack.translate((float) -currentPos.x() / scale_factor, (float) -currentPos.y() / scale_factor, (float) -currentPos.z() / scale_factor);
        } else if (!isOnShip) {
            poseStack.translate((float) -pos.getX() / scale_factor, (float) -pos.getY() / scale_factor, (float) -pos.getZ() / scale_factor);
        } else {
            Quaterniondc rot = getSubLevelRotation(ship).invert(new Quaterniond());
            poseStack.mulPose(new Quaternionf(rot.x(), rot.y(), rot.z(), rot.w()));

            AABB box = getSubLevelAABB(ship);
            if (box != null) {
                currentPos = new Vector3d(box.getCenter().x, box.getCenter().y, box.getCenter().z);
            } else {
                currentPos = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            }

            ResourceLocation currentDimension = Objects.requireNonNull(blockEntity.getLevel()).dimension().location();

            if (currentDimension.getPath().contains("wormhole")) {
                currentPos = currentPos.mul(32.0, new Vector3d());
            }

            poseStack.translate((float) -currentPos.x() / scale_factor, (float) -currentPos.y() / scale_factor, (float) -currentPos.z() / scale_factor);
        }

        if (registry != null) {
            for (Celestial body : registry) {
                renderCelestialProjection(poseStack, bufferSource, packedLight, packedOverlay, body, registry, isOnShip, currentPos, pos, scale_factor, blockRenderer, ticks, partialTick, body.type().equals(BuiltinCelestialTypes.STAR));
            }
        }

        poseStack.popPose();
    }

    private static void renderCelestialProjection(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Celestial celestial, Registry<Celestial> registry, boolean isOnShip, Vector3dc shipPos, BlockPos pos, int scale_factor, BlockRenderDispatcher blockRenderer, long ticks, float partialTick, boolean isStar) {
        Vector3d celestialPos = new Vector3d(celestial.getPosition(ticks, partialTick, registry));

        if (isOnShip) {
            if (shipPos != null && celestialPos.sub(new Vector3d(shipPos), new Vector3d()).length() > 120000) return;
        } else {
            if (celestialPos.sub(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), new Vector3d()).length() > 120000)
                return;
        }

        float scale = (float) (2 * Math.sqrt(celestial.getActualSize()) / Math.sqrt(scale_factor));
        if (scale > 0) {
            poseStack.translate(celestialPos.x / scale_factor, celestialPos.y / scale_factor, celestialPos.z / scale_factor);
            poseStack.scale(scale, scale, scale);

            Quaternionf rot = new Quaternionf(celestial.getRotation(ticks, partialTick, registry));
            poseStack.mulPose(rot);

            poseStack.translate(-0.5D, -0.5D, -0.5D);

            if (isStar) {
                blockRenderer.renderSingleBlock(Blocks.WHITE_STAINED_GLASS.defaultBlockState(),
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay
                );
            } else {
                blockRenderer.renderSingleBlock(Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
                        poseStack,
                        bufferSource,
                        packedLight,
                        packedOverlay
                );
            }

            poseStack.translate(0.5D, 0.5D, 0.5D);

            poseStack.mulPose(rot.invert());
            poseStack.scale(1 / scale, 1 / scale, 1 / scale);
            poseStack.translate(-celestialPos.x / scale_factor, -celestialPos.y / scale_factor, -celestialPos.z / scale_factor);
        }
    }

    private static Object getSableSubLevel(Level level, BlockPos pos) {
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Object subLevelsObj = null;

            try {
                Method getSubLevelsMethod = sableClass.getMethod("getSubLevels", Level.class);
                subLevelsObj = getSubLevelsMethod.invoke(null, level);
            } catch (Exception e1) {
                try {
                    Method getContainer = sableClass.getMethod("getSubLevelContainer", Level.class);
                    Object container = getContainer.invoke(null, level);
                    if (container != null) {
                        Method getAll = container.getClass().getMethod("getAllSubLevels");
                        subLevelsObj = getAll.invoke(container);
                    }
                } catch (Exception ignored) {
                }
            }

            if (subLevelsObj instanceof Iterable<?> subLevels) {
                for (Object sl : subLevels) {
                    if (sl == null) continue;
                    AABB box = getSubLevelAABB(sl);
                    if (box != null && box.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                        return sl;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static AABB getSubLevelAABB(Object subLevel) {
        if (subLevel == null) return null;
        try {
            Method boxMethod = subLevel.getClass().getMethod("getWorldAABB");
            Object obj = boxMethod.invoke(subLevel);
            if (obj instanceof AABB aabb) return aabb;
        } catch (Exception e) {
            try {
                Method boxMethod = subLevel.getClass().getMethod("getBoundingBox");
                Object obj = boxMethod.invoke(subLevel);
                if (obj instanceof AABB aabb) return aabb;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Quaterniondc getSubLevelRotation(Object subLevel) {
        if (subLevel == null) return new Quaterniond();
        try {
            Method rotMethod = subLevel.getClass().getMethod("getRotation");
            Object obj = rotMethod.invoke(subLevel);
            if (obj instanceof Quaterniondc q) return q;
        } catch (Exception e) {
            try {
                Method transformMethod = subLevel.getClass().getMethod("getTransform");
                Object transform = transformMethod.invoke(subLevel);
                if (transform != null) {
                    Method rotMethod = transform.getClass().getMethod("getShipToWorldRotation");
                    Object obj = rotMethod.invoke(transform);
                    if (obj instanceof Quaterniondc q) return q;
                }
            } catch (Exception ignored) {
            }
        }
        return new Quaterniond();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull NavProjectorBlockEntity blockEntity) {
        return true;
    }
}