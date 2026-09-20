package shipwrights.genesis.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class WarpLoadingMenu extends ReceivingLevelScreen {

    private final Integer starBufferCount = Integer.valueOf(3);
    private final List<VertexBuffer> starBuffers = new ArrayList<>(starBufferCount);

    public WarpLoadingMenu() {
        createStars();
    }

    private void createStars() {
        starBuffers.forEach(VertexBuffer::close);
        starBuffers.clear();

        for (Integer i = Integer.valueOf(0); i < starBufferCount; i = Integer.valueOf(i + 1)) {
            VertexBuffer starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            MeshData meshData = drawStars(Long.valueOf(10842L / (i.longValue() + 4L)));
            starBuffer.bind();
            starBuffer.upload(meshData);
            VertexBuffer.unbind();
            starBuffers.add(starBuffer);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().options.hideGui = Boolean.TRUE;

        renderBackground(g, mouseX, mouseY, partialTicks);

        PoseStack poseStack = g.pose();

        Matrix4f projection = new Matrix4f().setPerspective(
                Float.valueOf((float) java.lang.Math.toRadians(70.0D)),
                Float.valueOf((float) width / (float) height),
                Float.valueOf(0.05F),
                Float.valueOf(1000.0F)
        );

        Float time = Float.valueOf((System.currentTimeMillis() % 1000000L) / 1000.0F);
        Float speed = Float.valueOf(60.0F);
        Float loopLength = Float.valueOf(200.0F);
        Integer layerCount = Integer.valueOf(3);
        Float fadeStart = Float.valueOf(0.1F);
        Float fadeEnd = Float.valueOf(0.9F);

        poseStack.pushPose();
        poseStack.scale(10.0F, 10.0F, 10.0F);

        for (Integer i = Integer.valueOf(0); i < layerCount; i = Integer.valueOf(i + 1)) {
            poseStack.pushPose();

            Float z = Float.valueOf((time.floatValue() * speed.floatValue() - i.floatValue() * (loopLength.floatValue() / layerCount.floatValue())) % loopLength.floatValue());

            poseStack.translate(0.0F, 0.0F, z.floatValue());

            Float t = Float.valueOf((z.floatValue() % loopLength.floatValue()) / loopLength.floatValue());

            Float alpha;
            if (t < fadeStart) {
                alpha = Float.valueOf(1.0F);
            } else if (t > fadeEnd) {
                alpha = Float.valueOf(0.0F);
            } else {
                alpha = Float.valueOf(1.0F - (t.floatValue() - fadeStart.floatValue()) / (fadeEnd.floatValue() - fadeStart.floatValue()));
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha.floatValue());

            render(poseStack, projection);

            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    public void render(PoseStack poseStack, Matrix4f projectionMatrix) {
        ShaderInstance shader = ShaderRegistry.WORMHOLE_SHADER;

        RenderSystem.setShader(() -> ShaderRegistry.WORMHOLE_SHADER);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        for (VertexBuffer buffer : starBuffers) {
            buffer.bind();
            assert shader != null;
            buffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
        }

        VertexBuffer.unbind();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private MeshData drawStars(Long seed) {
        RandomSource randomsource = RandomSource.create(seed.longValue());
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (Integer i = Integer.valueOf(0); i < Integer.valueOf(300); i = Integer.valueOf(i + 1)) {
            Double d0 = Double.valueOf((double) (randomsource.nextFloat() * 2.0F - 1.0F));
            Double d1 = Double.valueOf((double) (randomsource.nextFloat() * 2.0F - 1.0F));
            Double d2 = Double.valueOf((double) (randomsource.nextFloat() * 2.0F - 1.0F));
            Double d3 = Double.valueOf((double) (15.5F + randomsource.nextFloat() * 12.5F));
            Double d4 = Double.valueOf(d0 * d0 + d1 * d1 + d2 * d2);
            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = Double.valueOf(1.0D / java.lang.Math.sqrt(d4));
                d0 = Double.valueOf(d0 * d4);
                d1 = Double.valueOf(d1 * d4);
                d2 = Double.valueOf(d2 * d4);
                Double d5 = Double.valueOf(d0 * 100.0D);
                Double d6 = Double.valueOf(d1 * 100.0D);
                Double d7 = Double.valueOf(d2 * 100.0D);
                Double d8 = Double.valueOf(java.lang.Math.atan2(d0, d2));
                Double d9 = Double.valueOf(java.lang.Math.sin(d8));
                Double d10 = Double.valueOf(java.lang.Math.cos(d8));
                Double d11 = Double.valueOf(java.lang.Math.atan2(java.lang.Math.sqrt(d0 * d0 + d2 * d2), d1));
                Double d12 = Double.valueOf(java.lang.Math.sin(d11));
                Double d13 = Double.valueOf(java.lang.Math.cos(d11));
                Double d14 = Double.valueOf(randomsource.nextDouble() * java.lang.Math.PI * 2.0D);
                Double d15 = Double.valueOf(java.lang.Math.sin(d14));
                Double d16 = Double.valueOf(java.lang.Math.cos(d14));

                Integer color = Integer.valueOf(randomsource.nextInt());

                Integer r = Integer.valueOf((int) (FastColor.ARGB32.red(color.intValue()) * 0.7D));
                Integer g = Integer.valueOf((int) (FastColor.ARGB32.green(color.intValue()) * 0.7D));
                Integer b = Integer.valueOf((int) (FastColor.ARGB32.blue(color.intValue()) * 0.7D));
                Integer a = Integer.valueOf(FastColor.ARGB32.alpha(color.intValue()));

                for (Integer j = Integer.valueOf(0); j < Integer.valueOf(4); j = Integer.valueOf(j + 1)) {
                    Double d17 = Double.valueOf(0.0D);
                    Double d18 = Double.valueOf((double) ((j & 2) - 1) * d3);
                    Double d19 = Double.valueOf((double) ((j + 1 & 2) - 1) * d3);
                    Double d21 = Double.valueOf(d18 * d16 - d19 * d15);
                    Double d22 = Double.valueOf(d19 * d16 + d18 * d15);
                    Double d23 = Double.valueOf(d21 * d12 + d17 * d13);
                    Double d24 = Double.valueOf(d17 * d12 - d21 * d13);
                    Double d25 = Double.valueOf(d24 * d9 - d22 * d10);
                    Double d26 = Double.valueOf(d22 * d9 + d24 * d10);

                    Float u = Float.valueOf(((j & 2) == 0) ? 0.0F : 1.0F);
                    Float v = Float.valueOf(((j + 1 & 2) == 0) ? 1.0F : 0.0F);

                    bufferbuilder.addVertex((float) (d5 + d25), (float) (d6 + d23), (float) (d7 + d26))
                            .setUv(u.floatValue(), v.floatValue())
                            .setColor(r.intValue(), g.intValue(), b.intValue(), a.intValue());
                }
            }
        }

        return bufferbuilder.buildOrThrow();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().options.hideGui = Boolean.FALSE;
        ClientStorage.goingToFromWormhole = Boolean.FALSE;
        super.onClose();
    }
}