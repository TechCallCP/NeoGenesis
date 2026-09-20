package shipwrights.genesis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import shipwrights.genesis.NeoGenesisMod;

@Mixin(ChunkBorderRenderer.class)
public class ChunkBorderRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    public void renderMixin(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !NeoGenesisMod.isMiniScale(level)) {
            return;
        }

        Entity entity = minecraft.gameRenderer.getMainCamera().getEntity();
        if (entity == null) {
            return;
        }

        double entityX = entity.getX();
        double entityY = entity.getY();
        double entityZ = entity.getZ();

        int centerX = (int) Math.floor(entityX);
        int centerY = (int) Math.floor(entityY);
        int centerZ = (int) Math.floor(entityZ);

        Matrix4f matrix4f = poseStack.last().pose();

        VertexConsumer redConsumer = bufferSource.getBuffer(RenderType.debugLineStrip(1.0F));

        int maxX = centerX + 1;
        int maxY = centerY + 1;
        int maxZ = centerZ + 1;

        float x0 = (float) (centerX - camX);
        float y0 = (float) (centerY - camY);
        float z0 = (float) (centerZ - camZ);
        float x1 = (float) (maxX - camX);
        float y1 = (float) (maxY - camY);
        float z1 = (float) (maxZ - camZ);

        addV(redConsumer, matrix4f, x0, y0, z0, 1.0F, 0.0F, 0.0F, 0.0F);
        addV(redConsumer, matrix4f, x0, y0, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y0, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y0, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y0, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y0, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y1, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y1, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y1, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y1, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y1, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y1, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y0, z0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y0, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x1, y1, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y1, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y0, z1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, x0, y0, z1, 1.0F, 0.0F, 0.0F, 0.0F);

        int bigMinX = centerX - 1;
        int bigMinY = centerY - 1;
        int bigMinZ = centerZ - 1;
        int bigMaxX = centerX + 2;
        int bigMaxY = centerY + 2;
        int bigMaxZ = centerZ + 2;

        float bx0 = (float) (bigMinX - camX);
        float by0 = (float) (bigMinY - camY);
        float bz0 = (float) (bigMinZ - camZ);
        float bx1 = (float) (bigMaxX - camX);
        float by1 = (float) (bigMaxY - camY);
        float bz1 = (float) (bigMaxZ - camZ);

        addV(redConsumer, matrix4f, bx0, by0, bz0, 1.0F, 0.0F, 0.0F, 0.0F);
        addV(redConsumer, matrix4f, bx0, by0, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by0, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by0, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by0, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by0, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by1, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by1, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by1, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by1, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by1, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by1, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by0, bz0, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by0, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx1, by1, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by1, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by0, bz1, 1.0F, 0.0F, 0.0F, 0.5F);
        addV(redConsumer, matrix4f, bx0, by0, bz1, 1.0F, 0.0F, 0.0F, 0.0F);

        VertexConsumer yellowConsumer = bufferSource.getBuffer(RenderType.debugLineStrip(1.0F));

        for (int i = 1; i < 4; i++) {
            float offset = i / 4.0F;
            addV(yellowConsumer, matrix4f, x0, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0 + offset, y0, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0 + offset, y1, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x0, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x0, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z0, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y0 + offset, z1, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x1, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
            addV(yellowConsumer, matrix4f, x1, y0, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.5F);
            addV(yellowConsumer, matrix4f, x1, y1, z0 + offset, 1.0F, 1.0F, 0.0F, 0.0F);
        }

        ci.cancel();
    }

    private static void addV(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }
}