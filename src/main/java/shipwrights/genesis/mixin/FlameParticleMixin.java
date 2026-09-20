package shipwrights.genesis.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import shipwrights.genesis.NeoGenesisMod;

import java.lang.reflect.Method;

@Mixin(FlameParticle.class)
public class FlameParticleMixin {

    @WrapOperation(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/AABB;move(DDD)Lnet/minecraft/world/phys/AABB;"
            ),
            remap = false
    )
    public AABB wrapMove(AABB instance, double x, double y, double z, Operation<AABB> original) {
        ClientLevel level = Minecraft.getInstance().level;
        double scale = 1.0;

        if (level != null) {
            try {
                // Attempt to resolve dynamically for when NeoGenesisMod is updated to accept the base Level class
                Method method = NeoGenesisMod.class.getMethod("getDimensionScale", Level.class);
                Object result = method.invoke(null, level);
                if (result instanceof Number num) {
                    scale = num.doubleValue();
                }
            } catch (Exception e1) {
                try {
                    // Fallback attempt: check if it accepts a dimension ResourceKey instead
                    Method method = NeoGenesisMod.class.getMethod("getDimensionScale", net.minecraft.resources.ResourceKey.class);
                    Object result = method.invoke(null, level.dimension());
                    if (result instanceof Number num) {
                        scale = num.doubleValue();
                    }
                } catch (Exception ignored) {
                    // Failsafe: return 1.0 scale on the client if the method strictly requires ServerLevel
                }
            }
        }

        return original.call(instance, x * scale, y * scale, z * scale);
    }
}