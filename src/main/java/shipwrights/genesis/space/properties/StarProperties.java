package shipwrights.genesis.space.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StarProperties(
        int r0,
        int g0,
        int b0,
        int r1,
        int g1,
        int b1
) implements CelestialProperties {
    public static final Codec<StarProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("r0").forGetter(StarProperties::r0),
            Codec.INT.fieldOf("g0").forGetter(StarProperties::g0),
            Codec.INT.fieldOf("b0").forGetter(StarProperties::b0),
            Codec.INT.fieldOf("r1").forGetter(StarProperties::r1),
            Codec.INT.fieldOf("g1").forGetter(StarProperties::g1),
            Codec.INT.fieldOf("b1").forGetter(StarProperties::b1)
    ).apply(instance, StarProperties::new));

    public static final StreamCodec<ByteBuf, StarProperties> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StarProperties::r0,
            ByteBufCodecs.VAR_INT, StarProperties::g0,
            ByteBufCodecs.VAR_INT, StarProperties::b0,
            ByteBufCodecs.VAR_INT, StarProperties::r1,
            ByteBufCodecs.VAR_INT, StarProperties::g1,
            ByteBufCodecs.VAR_INT, StarProperties::b1,
            StarProperties::new
    );
}