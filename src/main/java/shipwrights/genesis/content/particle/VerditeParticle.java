package shipwrights.genesis.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class VerditeParticle extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private int initialLifetime = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private static final float FALL_ACC = 0.25F;
    private static final float WIND_BIG = 2.0F;
    private float rotSpeed;
    private final float particleRandom;

    protected VerditeParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.setSprite(sprites.get(this.random.nextInt(4), 4));
        this.rotSpeed = (float) Math.toRadians(this.random.nextBoolean() ? -30.0F : 30.0F);
        this.particleRandom = this.random.nextFloat();
        initialLifetime += (int) Math.floor(this.random.nextFloat() * 50);
        this.lifetime = initialLifetime;
        float size = this.random.nextBoolean() ? 0.4F : 0.5F;
        this.quadSize = size;
        this.setSize(size, size);
        this.friction = 1.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.lifetime-- <= 0) {
            this.remove();
        }

        if (!this.removed) {
            float progress = (float) (initialLifetime - this.lifetime);
            float normalizedProgress = Math.min(progress / initialLifetime, 1.0F);
            double delta = Math.cos(Math.toRadians((this.particleRandom - 0.5F) * 60.0F)) * 2.0D * Math.pow(normalizedProgress, 1.25D);
            double deltaY = Math.sin(Math.toRadians((this.particleRandom - 0.5F) * 60.0F)) * 2.0D * Math.pow(normalizedProgress, 1.25D);
            double deltaZ = Math.sin(Math.toRadians((this.particleRandom - 0.5F) * 60.0F)) * 2.0D * Math.pow(normalizedProgress, 1.25D);

            this.xd += delta * 0.00025D;
            this.zd += deltaY * 0.00025D;
            this.yd += deltaZ * 0.00025D;
            this.move(this.xd, this.yd, this.zd);

            if (!this.removed) {
                this.xd *= this.friction;
                this.yd *= this.friction;
                this.zd *= this.friction;
            }
        }
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            VerditeParticle verditeParticle = new VerditeParticle(level, x, y, z, this.sprite);
            verditeParticle.pickSprite(this.sprite);
            return verditeParticle;
        }
    }
}
