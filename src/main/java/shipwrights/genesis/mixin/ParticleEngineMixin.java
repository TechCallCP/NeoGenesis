package shipwrights.genesis.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import shipwrights.genesis.NeoGenesisMod;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow(remap = false)
    public abstract void add(Particle particle);

    @Shadow(remap = false)
    private Particle makeParticle(ParticleOptions options, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return null;
    }

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true, remap = false)
    public void createParticleMixin(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> cir) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && NeoGenesisMod.isMiniScale(level)) {
            Particle particle = this.makeParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
            if (particle != null) {
                particle.scale(1 / 16f);
                this.add(particle);
                cir.setReturnValue(particle);
            } else {
                cir.setReturnValue(null);
            }
        }
    }

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true, remap = false)
    public void destroyMixin(BlockPos pos, BlockState state, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && NeoGenesisMod.isMiniScale(level)) {
            for (int i = 0; i < 10; i++) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        pos.getX() + Math.random(),
                        pos.getY() + Math.random(),
                        pos.getZ() + Math.random(),
                        0,
                        0,
                        0
                );
            }
            ci.cancel();
        }
    }

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true, remap = false)
    public void crackMixin(BlockPos pos, Direction side, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || NeoGenesisMod.isMiniScale(level)) {
            ci.cancel();
        }
    }
}