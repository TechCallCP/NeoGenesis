package shipwrights.genesis.space.transformProvider;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import shipwrights.genesis.space.Celestial;

import java.util.Random;

/**
 * Transform provider that simulates orbital mechanics for celestial bodies.
 * <br>
 * Uses the seed parameter to generate random but deterministic values for
 * orbital angles and base rotation (matching OrbitingBody behavior).
 */
public class OrbitingTransformProvider implements CelestialTransformProvider {
    public static final ResourceLocation TYPE = ResourceLocation.parse("genesis:orbiting");

    private final ResourceLocation parentID;
    private final int seed;

    // Configurable orbital parameters (from OrbitingBody)
    private final double orbitDistance;
    private final double orbitTime;
    private final double dayLength;

    // Random parameters derived from seed
    private final double orbitalTheta;
    private final double orbitalPhi;
    private final Quaterniondc baseRotation;

    /**
     * Creates an orbiting transform provider with specified orbital parameters.
     * The seed is used to generate random orbital angles and rotation.
     *
     * @param parentID the parent celestial body to orbit around
     * @param seed the seed for generating deterministic random orbital angles and rotation
     * @param orbitDistance the orbit radius in blocks
     * @param orbitTime the orbit period in ticks
     * @param dayLength the day length in ticks
     */
    public OrbitingTransformProvider(ResourceLocation parentID, int seed, double orbitDistance, double orbitTime, double dayLength) {
        this.parentID = parentID;
        this.seed = seed;
        this.orbitDistance = orbitDistance;
        this.orbitTime = orbitTime;
        this.dayLength = dayLength;

        // Generate random parameters from seed
        Random rand = new Random(seed);

        for (int i = 0; i < rand.nextInt(10); i++) {
            rand.nextDouble();
        }

        this.baseRotation = new Quaterniond();
        this.orbitalTheta = rand.nextDouble() * 2 * Math.PI;
        this.orbitalPhi = Math.PI / 2;
    }

    private Celestial getParent(Registry<Celestial> registry) {
        Celestial parent = registry.get(parentID);
        if (parent == null) throw new IllegalStateException("Parent celestial not found in registry: " + parentID);
        return parent;
    }

    private int getYearLengthTicks() {
        return (int)(this.orbitTime);
    }

    @Override
    public Quaterniondc getRotation(long ticks, float subticks, Registry<Celestial> registry) {
        if (this.dayLength == 0.0) {
            // Tidally locked: -Z side always faces the parent
            Vector3d myPos = getPosition(ticks, subticks, registry);
            Vector3d parentPos = new Vector3d(getParent(registry).getPosition(ticks, subticks, registry));
            Vector3d toParent = parentPos.sub(myPos, new Vector3d()).normalize();
            return new Quaterniond().rotateTo(new Vector3d(0, 0.8, -0.5).normalize(), toParent);
        }

        double rotationalPeriod = (this.orbitTime / (this.orbitTime / this.dayLength - 1.0));
        return new Quaterniond(baseRotation).rotateY(
                -Math.PI * 2 * (ticks + subticks) / rotationalPeriod
        );
    }

    @Override
    public Vector3d getPosition(long ticks, float subticks, Registry<Celestial> registry) {
        Vector3d out = new Vector3d(1, 0, 0);

        int yearLength = getYearLengthTicks();

        if (yearLength <= 0) {
            throw new IllegalStateException("YearLength should be > 0");
        }

        out = out.rotateY(Math.PI * 2 * (ticks + subticks) / yearLength);

        out = out.rotateY(orbitalTheta);
        out = out.rotateX(orbitalPhi + Math.PI / 2);

        out.normalize(orbitDistance);

        return out.add(getParent(registry).getPosition(ticks, subticks, registry), new Vector3d()).setComponent(1, 0);
    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    public static final MapCodec<OrbitingTransformProvider> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("parentID").forGetter(p -> p.parentID),
                    com.mojang.serialization.Codec.INT.fieldOf("seed").forGetter(p -> p.seed),
                    com.mojang.serialization.Codec.DOUBLE.fieldOf("orbitDistance").forGetter(p -> p.orbitDistance),
                    com.mojang.serialization.Codec.DOUBLE.fieldOf("orbitTime").forGetter(p -> p.orbitTime),
                    com.mojang.serialization.Codec.DOUBLE.optionalFieldOf("dayLength", 1.0).forGetter(p -> p.dayLength)
            ).apply(instance, OrbitingTransformProvider::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, OrbitingTransformProvider> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, p -> p.parentID,
            ByteBufCodecs.VAR_INT, p -> p.seed,
            ByteBufCodecs.DOUBLE, p -> p.orbitDistance,
            ByteBufCodecs.DOUBLE, p -> p.orbitTime,
            ByteBufCodecs.DOUBLE, p -> p.dayLength,
            OrbitingTransformProvider::new
    );

    public static void register() {
        CelestialTransformProvider.register(TYPE, CODEC, STREAM_CODEC);
    }
}