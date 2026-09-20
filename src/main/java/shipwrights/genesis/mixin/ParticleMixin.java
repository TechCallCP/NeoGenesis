package shipwrights.genesis.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;

import shipwrights.genesis.NeoGenesisMod;

@Mixin(Particle.class)
public class ParticleMixin {

    @WrapMethod(method = "move", remap = false)
    public void wrapMove(double x, double y, double z, Operation<Void> original) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && NeoGenesisMod.isMiniScale(level)) {
            original.call(x / 16.0, y / 16.0, z / 16.0);
        } else {
            original.call(x, y, z);
        }
    }
}