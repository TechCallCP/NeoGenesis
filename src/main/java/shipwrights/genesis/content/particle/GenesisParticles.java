package shipwrights.genesis.content.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import shipwrights.genesis.NeoGenesisMod;

public class GenesisParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, NeoGenesisMod.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ZAP_BUBBLE_PARTICLES =
            PARTICLES.register("zap_bubble_particles", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VERDITE_PARTICLES =
            PARTICLES.register("verdite_particles", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}