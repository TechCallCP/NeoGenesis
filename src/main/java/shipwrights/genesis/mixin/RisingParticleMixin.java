package shipwrights.genesis.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import shipwrights.genesis.NeoGenesisMod;

import java.lang.reflect.Method;

@Mixin(RisingParticle.class)
public abstract class RisingParticleMixin {

    @ModifyConstant(
            method = "<init>",
            constant = @Constant(floatValue = 0.05F)
    )
    private float genesis$reduceRandomOffset(float original, ClientLevel p_107631_, double p_107632_, double p_107633_, double p_107634_, double p_107635_, double p_107636_, double p_107637_) {
        double scale = 1.0;

        try {
            // Attempt to resolve dynamically for when NeoGenesisMod is updated to accept the base Level class
            Method method = NeoGenesisMod.class.getMethod("getDimensionScale", Level.class);
            Object result = method.invoke(null, p_107631_);
            if (result instanceof Number num) {
                scale = num.doubleValue();
            }
        } catch (Exception e1) {
            try {
                // Fallback attempt: check if it accepts a dimension ResourceKey instead
                Method method = NeoGenesisMod.class.getMethod("getDimensionScale", net.minecraft.resources.ResourceKey.class);
                Object result = method.invoke(null, p_107631_.dimension());
                if (result instanceof Number num) {
                    scale = num.doubleValue();
                }
            } catch (Exception ignored) {
                // Failsafe: return 1.0 scale on the client if the method strictly requires ServerLevel
            }
        }

        return (float) (original * scale);
    }
}