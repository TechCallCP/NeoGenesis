package shipwrights.genesis.space.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Atmosphere(
        double density,
        double thickness,
        boolean precipitation,
        boolean isBreathable,
        PlanetColorPalette color
) {
    public static final Codec<Atmosphere> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("density").forGetter(Atmosphere::density),
            Codec.DOUBLE.fieldOf("thickness").forGetter(Atmosphere::thickness),
            Codec.BOOL.fieldOf("precipitation").forGetter(Atmosphere::precipitation),
            Codec.BOOL.fieldOf("isBreathable").forGetter(Atmosphere::isBreathable),
            PlanetColorPalette.CODEC.fieldOf("color").forGetter(Atmosphere::color)
    ).apply(instance, Atmosphere::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Atmosphere> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Atmosphere::density,
            ByteBufCodecs.DOUBLE, Atmosphere::thickness,
            ByteBufCodecs.BOOL, Atmosphere::precipitation,
            ByteBufCodecs.BOOL, Atmosphere::isBreathable,
            PlanetColorPalette.STREAM_CODEC, Atmosphere::color,
            Atmosphere::new
    );
}