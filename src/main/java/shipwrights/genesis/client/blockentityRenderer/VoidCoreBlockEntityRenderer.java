package shipwrights.genesis.client.blockentityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import shipwrights.genesis.content.block.VoidCoreBlock;
import shipwrights.genesis.content.blockentity.VoidCoreBlockEntity;

public class VoidCoreBlockEntityRenderer implements BlockEntityRenderer<VoidCoreBlockEntity> {

    private static final ResourceLocation PORTAL_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/end_portal.png");
    private static final float PORTAL_SIZE = 0.5F;

    public VoidCoreBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(VoidCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();

        if (state.hasProperty(VoidCoreBlock.DORMANT)) {
            if (state.getValue(VoidCoreBlock.DORMANT)) {
                return;
            }
        }
        poseStack.pushPose();

        poseStack.translate(0.5D, 0.5D, 0.5D);

        float scale = 1.0F;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.endPortal());
        Matrix4f matrix = poseStack.last().pose();

        drawPortalFace(vertexConsumer, matrix, scale, PORTAL_SIZE, 0, 0, 1);
        drawPortalFace(vertexConsumer, matrix, scale, PORTAL_SIZE, 0, 0, -1);
        drawPortalFace(vertexConsumer, matrix, scale, PORTAL_SIZE, 0, 1, 0);
        drawPortalFace(vertexConsumer, matrix, scale, PORTAL_SIZE, 0, -1, 0);
        drawPortalFace(vertexConsumer, matrix, scale, PORTAL_SIZE, 1, 0, 0);
        drawPortalFace(vertexConsumer, matrix, scale, PORTAL_SIZE, -1, 0, 0);

        poseStack.popPose();
    }

    private void drawPortalFace(VertexConsumer consumer, Matrix4f matrix,
                                float scale, float size, float normalX, float normalY, float normalZ) {
        PoseStack tempStack = new PoseStack();
        if (normalX != 0) {
            tempStack.mulPose(new Quaternionf().rotationY((float) Math.PI / 2.0F));
        } else if (normalY != 0) {
            tempStack.mulPose(new Quaternionf().rotationX((float) Math.PI / 2.0F));
        }
        Matrix4f rotatedMatrix = new Matrix4f(matrix).mul(tempStack.last().pose());

        float x = size * normalX;
        float y = size * normalY;
        float z = size * normalZ;

        if (normalX > 0 || normalY > 0 || normalZ > 0) {
            consumer.addVertex(rotatedMatrix, -size, -size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(0.0F, 0.0F);
            consumer.addVertex(rotatedMatrix, size, -size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(1.0F, 0.0F);
            consumer.addVertex(rotatedMatrix, size, size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(1.0F, 1.0F);
            consumer.addVertex(rotatedMatrix, -size, size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(0.0F, 1.0F);
        } else {
            consumer.addVertex(rotatedMatrix, -size, -size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(0.0F, 0.0F);
            consumer.addVertex(rotatedMatrix, -size, size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(0.0F, 1.0F);
            consumer.addVertex(rotatedMatrix, size, size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(1.0F, 1.0F);
            consumer.addVertex(rotatedMatrix, size, -size, x + y + z)
                    .setColor(0, 0, 0, 255)
                    .setUv(1.0F, 0.0F);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(VoidCoreBlockEntity blockEntity) {
        return false;
    }
}
