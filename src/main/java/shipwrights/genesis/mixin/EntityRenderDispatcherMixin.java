package shipwrights.genesis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import shipwrights.genesis.NeoGenesisMod;

@Mixin(value = EntityRenderDispatcher.class, remap = false)
public class EntityRenderDispatcherMixin {

    @Unique
    private static final ThreadLocal<Entity> genesis$currentEntity = new ThreadLocal<>();

    @Inject(method = "renderHitbox", at = @At("HEAD"), remap = false)
    private static void captureEntity(
            PoseStack poseStack,
            VertexConsumer buffer,
            Entity entity,
            float red,
            float green,
            float blue,
            float partialTick,
            CallbackInfo ci
    ) {
        genesis$currentEntity.set(entity);
    }

    @Inject(method = "renderHitbox", at = @At("RETURN"), remap = false)
    private static void clearEntity(
            PoseStack poseStack,
            VertexConsumer buffer,
            Entity entity,
            float red,
            float green,
            float blue,
            float partialTick,
            CallbackInfo ci
    ) {
        genesis$currentEntity.remove();
    }

    @ModifyConstant(
            method = "renderHitbox",
            constant = @Constant(doubleValue = 2.0),
            remap = false
    )
    private static double shortenBlueLineInSpace(double original) {
        Entity entity = genesis$currentEntity.get();
        if (entity != null && NeoGenesisMod.isMiniScale(entity.level())) {
            return original / 16.0;
        }
        return original;
    }
}