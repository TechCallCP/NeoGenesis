package shipwrights.genesis.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import shipwrights.genesis.NeoGenesisMod;

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
            scale = NeoGenesisMod.getDimensionScale(level);
        }

        return original.call(instance, x * scale, y * scale, z * scale);
    }
}