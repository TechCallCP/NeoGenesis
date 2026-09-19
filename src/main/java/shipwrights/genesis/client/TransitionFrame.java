package shipwrights.genesis.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@OnlyIn(Dist.CLIENT)
public class TransitionFrame {
    public static RenderTarget capturedTarget;

    public static void init(int width, int height) {
        if (capturedTarget != null) {
            capturedTarget.destroyBuffers();
        }

        capturedTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        capturedTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    }

    public static void captureFrame() {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();

        if (capturedTarget == null ||
                capturedTarget.width != main.width ||
                capturedTarget.height != main.height) {

            init(main.width, main.height);
        }

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, capturedTarget.frameBufferId);

        GL30.glBlitFramebuffer(
                0, 0, main.width, main.height,
                0, 0, capturedTarget.width, capturedTarget.height,
                GL11.GL_COLOR_BUFFER_BIT,
                GL11.GL_NEAREST
        );

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, main.frameBufferId);
    }
}
