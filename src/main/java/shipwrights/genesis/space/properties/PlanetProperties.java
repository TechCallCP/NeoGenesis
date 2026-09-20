package shipwrights.genesis.space.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PlanetProperties(
        Atmosphere atmosphere
) implements CelestialProperties {
    public static final Codec<PlanetProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Atmosphere.CODEC.fieldOf("atmosphere").forGetter(PlanetProperties::atmosphere)
    ).apply(instance, PlanetProperties::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlanetProperties> STREAM_CODEC = Atmosphere.STREAM_CODEC.map(
            PlanetProperties::new,
            PlanetProperties::atmosphere
    );
}