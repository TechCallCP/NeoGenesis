package shipwrights.genesis.client.blockentityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import org.joml.Matrix4f;

import shipwrights.genesis.content.block.RadarDisplayBlock;
import shipwrights.genesis.content.blockentity.RadarDisplayBlockEntity;

public class RadarDisplayBlockEntityRenderer implements BlockEntityRenderer<RadarDisplayBlockEntity> {

    private static final double DEPTH_NEAR = 0.0;
    private static final double DEPTH_FAR = 25_000.0;

    public RadarDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(RadarDisplayBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        double[][] data = blockEntity.getDisplayableData();
        int resolution = data.length;

        if (resolution == 0) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        Direction facing = blockEntity.getBlockState().getValue(RadarDisplayBlock.FACING);
        rotateToFacing(poseStack, facing);

        poseStack.translate(0.0, 0.0, 0.501);

        float pixelSize = 1.0F / resolution;

        VertexConsumer vc = bufferSource.getBuffer(RenderType.debugQuads());

        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {

                int[] rgb = getColor(data[x][y]);
                float r = rgb[0] / 255.0F;
                float g = rgb[1] / 255.0F;
                float b = rgb[2] / 255.0F;

                float x0 = 0.5F - x * pixelSize;
                float y0 = -0.5F + y * pixelSize;
                float x1 = x0 - pixelSize;
                float y1 = y0 + pixelSize;

                addQuad(
                        poseStack.last().pose(),
                        vc,
                        x0, y0, 0.0F,
                        x1, y1, 0.0F,
                        r, g, b
                );
            }
        }

        poseStack.popPose();
    }

    private int[] getColor(double depth) {
        if (depth <= 0) {
            return new int[]{0, 0, 0};
        }

        double t = (depth - DEPTH_NEAR) / (DEPTH_FAR - DEPTH_NEAR);

        if (t >= 1.0) {
            return new int[]{0, 0, 0};
        }

        t = Math.max(0.0, Math.min(1.0, t));

        int r, g, b;

        if (t < 0.2) {
            double localT = t / 0.2;
            r = 255;
            g = (int) (165 * localT);
            b = 0;
        } else if (t < 0.4) {
            double localT = (t - 0.2) / 0.2;
            r = 255;
            g = (int) (165 + (255 - 165) * localT);
            b = 0;
        } else if (t < 0.6) {
            double localT = (t - 0.4) / 0.2;
            r = (int) (255 * (1.0 - localT));
            g = 255;
            b = 0;
        } else if (t < 0.8) {
            double localT = (t - 0.6) / 0.2;
            r = 0;
            g = (int) (255 * (1.0 - localT));
            b = (int) (255 * localT);
        } else {
            double localT = (t - 0.8) / 0.2;
            r = 0;
            g = 0;
            b = (int) (255 * (1.0 - localT));
        }

        return new int[]{r, g, b};
    }

    private void rotateToFacing(PoseStack poseStack, Direction facing) {
        switch (facing.getOpposite()) {
            case NORTH -> {}
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }
    }

    private void addQuad(Matrix4f m, VertexConsumer vc,
                         float x0, float y0, float z,
                         float x1, float y1, float z2,
                         float r, float g, float b) {

        vc.addVertex(m, x0, y0, z).setColor(r, g, b, 1.0F);
        vc.addVertex(m, x1, y0, z).setColor(r, g, b, 1.0F);
        vc.addVertex(m, x1, y1, z2).setColor(r, g, b, 1.0F);
        vc.addVertex(m, x0, y1, z2).setColor(r, g, b, 1.0F);
    }

    @Override
    public boolean shouldRenderOffScreen(RadarDisplayBlockEntity blockEntity) {
        return false;
    }
}
