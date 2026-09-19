package shipwrights.genesis.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.MeshData;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class WarpLoadingMenu extends ReceivingLevelScreen {

    private final int starBufferCount = 3;
    private final List<VertexBuffer> starBuffers = new ArrayList<>(starBufferCount);

    public WarpLoadingMenu() {
        super(() -> true, ReceivingLevelScreen.Reason.OTHER);
        createStars();
    }

    private void createStars() {
        starBuffers.forEach(VertexBuffer::close);
        starBuffers.clear();

        for (int i = 0; i < starBufferCount; i++) {
            VertexBuffer starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            MeshData meshData = drawStars(10842L / (i + 4));
            starBuffer.bind();
            starBuffer.upload(meshData);
            VertexBuffer.unbind();
            starBuffers.add(starBuffer);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        Minecraft.getInstance().options.hideGui = true;

        renderBackground(g, mouseX, mouseY, partialTicks);

        PoseStack poseStack = g.pose();

        Matrix4f projection = new Matrix4f().setPerspective(
                (float) Math.toRadians(70),
                (float) width / height,
                0.05f,
                1000f
        );

        float time = (System.currentTimeMillis() % 1000000L) / 1000f;
        float speed = 60f;
        float loopLength = 200f;
        int layerCount = 3;
        float fadeStart = 0.1f;
        float fadeEnd   = 0.9f;

        poseStack.pushPose();
        poseStack.scale(10f, 10f, 10f);

        for (int i = 0; i < layerCount; i++) {
            poseStack.pushPose();

            float z = (time * speed - i * (loopLength / layerCount)) % loopLength;

            poseStack.translate(0, 0, z);

            float t = (z % loopLength) / loopLength;

            float alpha;
            if (t < fadeStart) {
                alpha = 1f;
            } else if (t > fadeEnd) {
                alpha = 0f;
            } else {
                alpha = 1f - (t - fadeStart) / (fadeEnd - fadeStart);
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

            renderStars(poseStack, projection);

            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        poseStack.popPose();
    }

    public void renderStars(PoseStack poseStack, Matrix4f projectionMatrix) {
        ShaderInstance shader = ShaderRegistry.WORMHOLE_SHADER.getInstance().get();

        RenderSystem.setShader(() -> ShaderRegistry.WORMHOLE_SHADER.getInstance().get());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        for (VertexBuffer buffer : starBuffers) {
            buffer.bind();
            buffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
        }

        VertexBuffer.unbind();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private MeshData drawStars(long seed) {
        RandomSource randomsource = RandomSource.create(seed);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        for (int i = 0; i < 300; ++i) {
            double d0 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
            double d1 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
            double d2 = (double)(randomsource.nextFloat() * 2.0F - 1.0F);
            double d3 = (double)(15.5F + randomsource.nextFloat() * 12.5F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 < 1.0 && d4 > 0.01) {
                d4 = 1.0 / Math.sqrt(d4);
                d0 *= d4;
                d1 *= d4;
                d2 *= d4;
                double d5 = d0 * 100.0;
                double d6 = d1 * 100.0;
                double d7 = d2 * 100.0;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = randomsource.nextDouble() * Math.PI * 2.0;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                int color = randomsource.nextInt();

                int r = (int) (FastColor.ARGB32.red(color) * 0.7);
                int g = (int) (FastColor.ARGB32.green(color) * 0.7);
                int b = (int) (FastColor.ARGB32.blue(color) * 0.7);
                int a = FastColor.ARGB32.alpha(color);

                for (int j = 0; j < 4; ++j) {
                    double d17 = 0.0;
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + d17 * d13;
                    double d24 = d17 * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;

                    float u = ((j & 2) == 0) ? 0.0f : 1.0f;
                    float v = ((j + 1 & 2) == 0) ? 1.0f : 0.0f;

                    bufferbuilder.addVertex((float) (d5 + d25), (float) (d6 + d23), (float) (d7 + d26))
                            .setColor(r, g, b, a)
                            .setUv(u, v);
                }
            }
        }

        return bufferbuilder.buildOrThrow();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().options.hideGui = false;
        ClientStorage.goingToFromWormhole = false;
        super.onClose();
    }
}