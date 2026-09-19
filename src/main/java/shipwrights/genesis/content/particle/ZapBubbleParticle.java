package shipwrights.genesis.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class ZapBubbleParticle extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025F;
    private int initialLifetime = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private static final float FALL_ACC = 0.25F;
    private static final float WIND_BIG = 2.0F;
    private float rotSpeed;
    private final float particleRandom;
    private final float spinAcceleration;

    protected ZapBubbleParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.setSprite(sprites.get(this.random.nextInt(12), 12));
        this.rotSpeed = (float) Math.toRadians(this.random.nextBoolean() ? -30.0F : 30.0F);
        this.particleRandom = this.random.nextFloat();
        this.spinAcceleration = (float) Math.toRadians(this.random.nextBoolean() ? -5.0F : 5.0F);
        initialLifetime -= (int) Math.floor(particleRandom * 50);
        this.lifetime = initialLifetime;
        this.gravity = -7.5E-4F;
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
            spawnChild();
            this.remove();
        }

        if (!this.removed) {
            float progress = (float) (initialLifetime - this.lifetime);
            float normalizedProgress = Math.min(progress / initialLifetime, 1.0F);
            double deltaX = Math.cos(Math.toRadians(this.particleRandom * 60.0F)) * 2.0D * Math.pow(normalizedProgress, 1.25D);
            double deltaZ = Math.sin(Math.toRadians(this.particleRandom * 60.0F)) * 2.0D * Math.pow(normalizedProgress, 1.25D);

            this.xd += deltaX * 0.00025D;
            this.zd += deltaZ * 0.00025D;
            this.yd -= this.gravity;
            this.rotSpeed += this.spinAcceleration / 20.0F;
            this.oRoll = this.roll;
            this.roll += this.rotSpeed / 20.0F;
            this.move(this.xd, this.yd, this.zd);

            if (this.onGround || (this.lifetime < 299 && (this.xd == 0.0D || this.zd == 0.0D))) {
                spawnChild();
                this.remove();
            }

            if (!this.removed) {
                this.xd *= this.friction;
                this.yd *= this.friction;
                this.zd *= this.friction;
            }
        }
    }

    private void spawnChild() {
        this.level.addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                this.x,
                this.y,
                this.z,
                0.0D, 0.0D, 0.0D
        );
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ZapBubbleParticle zapBubbleParticle = new ZapBubbleParticle(level, x, y, z, this.sprite);
            zapBubbleParticle.pickSprite(this.sprite);
            return zapBubbleParticle;
        }
    }
}

