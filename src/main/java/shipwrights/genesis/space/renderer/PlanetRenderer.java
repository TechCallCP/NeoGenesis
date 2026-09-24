package shipwrights.genesis.space.renderer;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.client.PlanetDimensionEffects;
import shipwrights.genesis.client.ShaderRegistry;
import shipwrights.genesis.client.shading.FaceShadow;
import shipwrights.genesis.client.shading.ShadowProjection;
import shipwrights.genesis.client.shading.ShadowRenderer;
import shipwrights.genesis.math.AAPlane;
import shipwrights.genesis.math.OBB;
import shipwrights.genesis.mixin.FogRendererAccessor;
import shipwrights.genesis.mixin.LevelRendererAccessor;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.VantagePoint;

import java.util.ArrayList;
import java.util.List;

import static shipwrights.genesis.client.ShaderRegistry.getPlanetShadowRenderType;
import static shipwrights.genesis.client.ShaderRegistry.getTexturedPlanetRenderType;

public class PlanetRenderer implements CelestialRenderer {

    private static final boolean USE_TEST_SHADOWS = false;

    @Override
    public void teardown(@NotNull RenderLevelStageEvent event, @NotNull VantagePoint vantagePoint) {
        CelestialRenderer.super.teardown(event, vantagePoint);

        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public void invoke(@NotNull RenderLevelStageEvent event, @NotNull Celestial toRender, @NotNull VantagePoint vantagePoint) {
        ClientLevel level = ((LevelRendererAccessor) event.getLevelRenderer()).getLevel();
        long ticks = NeoGenesisMod.getTicks(level);
        float partialTick = NeoGenesisMod.getPartialTick(level, event);

        Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(level);
        Vector3dc position = toRender.getPosition(ticks, partialTick, registry);
        Quaterniondc rotation = toRender.getRotation(ticks, partialTick, registry);
        double halfExtent = toRender.getActualSize() / 2.0;
        float alpha = 1.0F;

        Vector3dc starPosition = toRender.getNearestStar(ticks, partialTick, registry).getPosition(ticks, partialTick, registry);

        List<FaceShadow> shadows;
        if (USE_TEST_SHADOWS) {
            shadows = createTestShadows(halfExtent);
        } else {
            OBB selfOBB = toRender.getOBB(ticks, partialTick, registry);
            List<Celestial> allCelestials = registry.stream().filter(it -> it.type().castsShadow() && !it.equals(toRender)).toList();
            List<OBB> otherOBBs = allCelestials.stream().map(it -> it.getOBB(ticks, partialTick, registry)).toList();

            shadows = ShadowProjection.computeShadows(selfOBB, otherOBBs, starPosition);
        }

        Vector3d lightDir = new Vector3d(position).sub(starPosition).rotate(vantagePoint.getRotation().conjugate(new Quaterniond()));
        ShaderInstance shader = ShaderRegistry.PLANET_TEXTURED_SHADER;
        if (shader != null) {
            shader.safeGetUniform("LightDirection").set((float) lightDir.x, (float) lightDir.y, (float) lightDir.z);
        }

        Vec3 skyColor = Vec3.ZERO;

        if (vantagePoint instanceof VantagePoint.OnCelestial oc && oc.celestial().equals(toRender)) {
            var camera = event.getCamera();
            halfExtent = Minecraft.getInstance().gameRenderer.getRenderDistance();
            position = new Vector3d(
                    0,
                    -(camera.getPosition().y / 16) - halfExtent - 32,
                    0
            );
            rotation = oc.cameraRotationFromNorthPole();
            int buildHeight = level.getMaxBuildHeight();
            float alphaInterpolateStart = (float) (halfExtent + buildHeight / 3.0F);
            float alphaInterpolateEnd = (float) (halfExtent + buildHeight);

            float cameraY = (float) camera.getPosition().y;
            alpha = Math.max(0.0F, Math.min(1.0F, (cameraY - alphaInterpolateStart) / (alphaInterpolateEnd - alphaInterpolateStart)));
        } else if (vantagePoint instanceof VantagePoint.InSpace) {
            Vec3 cameraPos = event.getCamera().getPosition();
            position = new Vector3d(position).sub(cameraPos.x, cameraPos.y, cameraPos.z);
        } else {
            skyColor = PlanetDimensionEffects.cachedSkyColor;

            Vector3dc vantagePos = vantagePoint.getPosition();
            Quaterniondc vantageRot = vantagePoint.getRotation();

            Vector3d relativePos = new Vector3d(
                    position.x() - vantagePos.x(),
                    position.y() - vantagePos.y(),
                    position.z() - vantagePos.z()
            );

            double starBrightness = PlanetDimensionEffects.cachedStarBrightness;

            if (starBrightness < 0.05 && relativePos.length() > 4096) return;

            Quaterniond planetRotation = new Quaterniond();

            Quaterniond inverseVantageRot = planetRotation.premul(vantageRot).conjugate();
            inverseVantageRot.transform(relativePos);
            position = relativePos;

            rotation = new Quaterniond(inverseVantageRot).mul(new Quaterniond(rotation));
        }

        if (shader != null) {
            shader.safeGetUniform("SkyColor").set((float) skyColor.x, (float) skyColor.y, (float) skyColor.z);
        }

        renderPlanetAt(registry.getResourceKey(toRender).orElseThrow().location(), shadows, event.getPoseStack(), position.x(), position.y(), position.z(), halfExtent, rotation, alpha);

        new PlanetAtmosphereRenderer().invoke(event, toRender, vantagePoint);
    }

    private void renderPlanetAt(ResourceLocation planetID, List<FaceShadow> shadows, PoseStack poseStack, double x, double y, double z, double halfExtent, Quaterniondc localRotation, float alpha) {
        ResourceLocation textureLocation = ResourceLocation.parse("neogenesis:planets/" + planetID.getNamespace() + "/" + planetID.getPath());

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var renderType = getTexturedPlanetRenderType(textureLocation);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        Matrix4f matrix = new Matrix4f(poseStack.last().pose());

        matrix.translate((float) x, (float) y, (float) z);

        Quaternionf rotation = new Quaternionf(localRotation);
        matrix.rotate(rotation);

        float halfSize = (float) halfExtent;

        float third = 1.0F / 3.0F;
        float twoThirds = 2.0F / 3.0F;

        addTexturedCubeFace(matrix, buffer, halfSize, new Vector3f(0.0F, 0.0F, 1.0F), rotation, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, twoThirds, 0.0F, 1.0F, 0.5F, alpha);
        addTexturedCubeFace(matrix, buffer, halfSize, new Vector3f(0.0F, 0.0F, -1.0F), rotation, halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, 0.0F, 0.0F, third, 0.5F, alpha);
        addTexturedCubeFace(matrix, buffer, halfSize, new Vector3f(-1.0F, 0.0F, 0.0F), rotation, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, -halfSize, third, 0.0F, twoThirds, 0.5F, alpha);
        addTexturedCubeFace(matrix, buffer, halfSize, new Vector3f(1.0F, 0.0F, 0.0F), rotation, halfSize, -halfSize, halfSize, halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, 0.0F, 0.5F, third, 1.0F, alpha);
        addTexturedCubeFace(matrix, buffer, halfSize, new Vector3f(0.0F, -1.0F, 0.0F), rotation, -halfSize, -halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, third, 0.5F, twoThirds, 1.0F, alpha);
        addTexturedCubeFace(matrix, buffer, halfSize, new Vector3f(0.0F, 1.0F, 0.0F), rotation, -halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize, -halfSize, halfSize, -halfSize, twoThirds, 0.5F, 1.0F, 1.0F, alpha);

        bufferSource.endBatch(renderType);

        MultiBufferSource.BufferSource bufferSource1 = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource1.endBatch();
        renderShadows(shadows, poseStack, x, y, z, halfExtent, localRotation);
        bufferSource1.endBatch();
    }

    private static void addTexturedCubeFace(Matrix4f matrix, VertexConsumer buffer, float halfSize,
                                            Vector3f normal, Quaternionf rotation,
                                            float x1, float y1, float z1,
                                            float x2, float y2, float z2,
                                            float x3, float y3, float z3,
                                            float x4, float y4, float z4,
                                            float u1, float v1, float u2, float v2, float alpha) {
        addTexturedVertexWithLighting(matrix, buffer, x1, y1, z1, u1, v2, normal, rotation, alpha);
        addTexturedVertexWithLighting(matrix, buffer, x2, y2, z2, u2, v2, normal, rotation, alpha);
        addTexturedVertexWithLighting(matrix, buffer, x3, y3, z3, u2, v1, normal, rotation, alpha);
        addTexturedVertexWithLighting(matrix, buffer, x4, y4, z4, u1, v1, normal, rotation, alpha);
    }

    private static void addTexturedVertexWithLighting(Matrix4f matrix, VertexConsumer buffer,
                                                      float x, float y, float z,
                                                      float u, float v,
                                                      Vector3f normal, Quaternionf rotation, float alpha) {
        int fogRed = (int) (255 * FogRendererAccessor.getFogRed());
        int fogGreen = (int) (255 * FogRendererAccessor.getFogGreen());
        int fogBlue = (int) (255 * FogRendererAccessor.getFogBlue());

        Vector3f rotatedNormal = new Vector3f(x, y, z);
        rotatedNormal = rotation.transform(rotatedNormal.normalize());

        buffer.addVertex(matrix, x, y, z)
                .setUv(u, v)
                .setColor(fogRed, fogGreen, fogBlue, (int) (alpha * 255))
                .setNormal(rotatedNormal.x, rotatedNormal.y, rotatedNormal.z);
    }

    private void renderShadows(List<FaceShadow> shadows, PoseStack poseStack, double x, double y, double z, double halfExtent, Quaterniondc localRotation) {
        if (shadows == null || shadows.isEmpty()) {
            return;
        }

        float sharedEdgeWidth = 0.0001F;
        for (FaceShadow shadow : shadows) {
            sharedEdgeWidth = Math.max(sharedEdgeWidth, ShadowRenderer.computeEdgeWidth(shadow));
        }

        for (FaceShadow shadow : shadows) {
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer shadowBuffer = bufferSource.getBuffer(getPlanetShadowRenderType());

            Matrix4f matrix = new Matrix4f(poseStack.last().pose());

            matrix.translate((float) x, (float) y, (float) z);
            matrix.rotate(new Quaternionf(localRotation));

            ShaderInstance shader = ShaderRegistry.PLANET_SHADOW_SHADER;
            if (shader != null) {
                List<Vector2dc> polygon = shadow.polygon();
                AAPlane plane = shadow.plane();

                int count = Math.min(polygon.size(), 8);

                Uniform countUniform = shader.getUniform("ShadowVertexCount");
                if (countUniform != null) {
                    countUniform.set((float) count);
                }

                int boundaryMask = 0;
                double eps = halfExtent * 1e-4;
                for (int i = 0; i < count; i++) {
                    int j = (i + 1) % count;
                    Vector2dc a = polygon.get(i);
                    Vector2dc b = polygon.get(j);
                    boolean onBoundary =
                            (Math.abs(a.x() - halfExtent) < eps && Math.abs(b.x() - halfExtent) < eps) ||
                                    (Math.abs(a.x() + halfExtent) < eps && Math.abs(b.x() + halfExtent) < eps) ||
                                    (Math.abs(a.y() - halfExtent) < eps && Math.abs(b.y() - halfExtent) < eps) ||
                                    (Math.abs(a.y() + halfExtent) < eps && Math.abs(b.y() + halfExtent) < eps);
                    if (onBoundary) {
                        boundaryMask |= (1 << i);
                    }
                }
                Uniform maskUniform = shader.getUniform("ShadowEdgeMask");
                if (maskUniform != null) {
                    maskUniform.set((float) boundaryMask);
                }

                Uniform edgeWidthUniform = shader.getUniform("ShadowEdgeWidth");
                if (edgeWidthUniform != null) {
                    edgeWidthUniform.set(sharedEdgeWidth);
                }

                for (int i = 0; i < 8; i++) {
                    Uniform u = shader.getUniform("ShadowVertex[" + i + "]");
                    if (u != null) {
                        if (i < count) {
                            Vector3d v3d = ShadowRenderer.applyZFightingOffset(
                                    ShadowRenderer.convertPlaneToLocal3D(polygon.get(i), plane),
                                    plane,
                                    halfExtent
                            );

                            Vector3f transformed = new Vector3f(
                                    (float) v3d.x,
                                    (float) v3d.y,
                                    (float) v3d.z
                            );
                            matrix.transformPosition(transformed);

                            u.set(transformed.x, transformed.y, transformed.z);
                        } else {
                            u.set(0.0F, 0.0F, 0.0F);
                        }
                    }
                }
            }

            ShadowRenderer.renderShadow(shadow, matrix, shadowBuffer, halfExtent);

            bufferSource.endBatch(getPlanetShadowRenderType());

            MultiBufferSource.BufferSource bufferSource1 = Minecraft.getInstance().renderBuffers().bufferSource();
            bufferSource1.endBatch();
        }
    }

    private static List<FaceShadow> createTestShadows(double halfExtent) {
        List<FaceShadow> testShadows = new ArrayList<>();

        AAPlane topPlane = new AAPlane(new Vector3i(0, 1, 0), halfExtent);
        List<Vector2dc> topSquare = List.of(
                new Vector2d(-halfExtent * 0.3, -halfExtent * 0.3),
                new Vector2d(halfExtent * 0.3, -halfExtent * 0.3),
                new Vector2d(halfExtent * 0.3, halfExtent * 0.3),
                new Vector2d(-halfExtent * 0.3, halfExtent * 0.3)
        );
        testShadows.add(new FaceShadow(topPlane, topSquare));

        AAPlane frontPlane = new AAPlane(new Vector3i(0, 0, 1), halfExtent);
        List<Vector2dc> frontTriangle = List.of(
                new Vector2d(0, -halfExtent * 0.4),
                new Vector2d(halfExtent * 0.4, halfExtent * 0.4),
                new Vector2d(-halfExtent * 0.4, halfExtent * 0.4)
        );
        testShadows.add(new FaceShadow(frontPlane, frontTriangle));

        AAPlane rightPlane = new AAPlane(new Vector3i(1, 0, 0), halfExtent);
        List<Vector2dc> rightPentagon = List.of(
                new Vector2d(0, -halfExtent * 0.4),
                new Vector2d(halfExtent * 0.3, -halfExtent * 0.2),
                new Vector2d(halfExtent * 0.3, halfExtent * 0.2),
                new Vector2d(0, halfExtent * 0.4),
                new Vector2d(-halfExtent * 0.3, 0)
        );
        testShadows.add(new FaceShadow(rightPlane, rightPentagon));

        AAPlane bottomPlane = new AAPlane(new Vector3i(0, -1, 0), -halfExtent);
        List<Vector2dc> bottomHexagon = List.of(
                new Vector2d(halfExtent * 0.3, 0),
                new Vector2d(halfExtent * 0.15, halfExtent * 0.3),
                new Vector2d(-halfExtent * 0.15, halfExtent * 0.3),
                new Vector2d(-halfExtent * 0.3, 0),
                new Vector2d(-halfExtent * 0.15, -halfExtent * 0.3),
                new Vector2d(halfExtent * 0.15, -halfExtent * 0.3)
        );
        testShadows.add(new FaceShadow(bottomPlane, bottomHexagon));

        AAPlane backPlane = new AAPlane(new Vector3i(0, 0, -1), -halfExtent);
        List<Vector2dc> backDiamond = List.of(
                new Vector2d(0, -halfExtent * 0.4),
                new Vector2d(halfExtent * 0.3, 0),
                new Vector2d(0, halfExtent * 0.4),
                new Vector2d(-halfExtent * 0.3, 0)
        );
        testShadows.add(new FaceShadow(backPlane, backDiamond));

        AAPlane leftPlane = new AAPlane(new Vector3i(-1, 0, 0), -halfExtent);
        List<Vector2dc> leftRectangle = List.of(
                new Vector2d(-halfExtent * 0.2, -halfExtent * 0.4),
                new Vector2d(halfExtent * 0.2, -halfExtent * 0.4),
                new Vector2d(halfExtent * 0.2, halfExtent * 0.4),
                new Vector2d(-halfExtent * 0.2, halfExtent * 0.4)
        );
        testShadows.add(new FaceShadow(leftPlane, leftRectangle));

        return testShadows;
    }
}
