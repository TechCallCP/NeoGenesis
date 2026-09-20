package shipwrights.genesis.space.properties;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Properties for celestial types that need no extra data (e.g. black holes). */
public record EmptyProperties() implements CelestialProperties {
    public static final EmptyProperties INSTANCE = new EmptyProperties();
    public static final Codec<EmptyProperties> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, EmptyProperties> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}