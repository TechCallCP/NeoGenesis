package shipwrights.genesis.space.transformProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import shipwrights.genesis.space.Celestial;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows custom position and rotation for celestial bodies.
 * <br>
 * Implementations must be registered via {@link #register(ResourceLocation, MapCodec, StreamCodec)} before use.
 */
public interface CelestialTransformProvider {

    Vector3d getPosition(long ticks, float subticks, Registry<Celestial> registry);

    Quaterniondc getRotation(long ticks, float subticks, Registry<Celestial> registry);

    ResourceLocation getType();

    // Registry maps for CelestialTransformProvider codecs and stream codecs
    Map<ResourceLocation, MapCodec<? extends CelestialTransformProvider>> CODEC_REGISTRY = new HashMap<>();
    Map<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, ? extends CelestialTransformProvider>> STREAM_CODEC_REGISTRY = new HashMap<>();

    /**
     * Register a CelestialTransformProvider type with its codec and stream codec.
     *
     * @param type The unique identifier for this provider type
     * @param codec The codec to serialize/deserialize this provider type
     * @param streamCodec The stream codec to sync this provider type over network
     */
    static void register(ResourceLocation type,
                         MapCodec<? extends CelestialTransformProvider> codec,
                         StreamCodec<RegistryFriendlyByteBuf, ? extends CelestialTransformProvider> streamCodec) {
        CODEC_REGISTRY.put(type, codec);
        STREAM_CODEC_REGISTRY.put(type, streamCodec);
    }

    /**
     * Dispatch codec that handles serialization of any registered CelestialTransformProvider type.
     */
    Codec<CelestialTransformProvider> CODEC = ResourceLocation.CODEC.dispatch(
            CelestialTransformProvider::getType,
            type -> {
                MapCodec<? extends CelestialTransformProvider> codec = CODEC_REGISTRY.get(type);
                if (codec == null) {
                    throw new IllegalArgumentException("Unknown CelestialTransformProvider type: " + type);
                }
                return codec;
            }
    );

    /**
     * Stream codec that handles network syncing of any registered CelestialTransformProvider type.
     */
    StreamCodec<RegistryFriendlyByteBuf, CelestialTransformProvider> STREAM_CODEC = StreamCodec.of(
            (buf, provider) -> {
                ResourceLocation type = provider.getType();
                buf.writeResourceLocation(type);
                @SuppressWarnings("unchecked")
                StreamCodec<RegistryFriendlyByteBuf, CelestialTransformProvider> streamCodec =
                        (StreamCodec<RegistryFriendlyByteBuf, CelestialTransformProvider>) STREAM_CODEC_REGISTRY.get(type);
                if (streamCodec == null) {
                    throw new IllegalArgumentException("Unknown CelestialTransformProvider stream codec for type: " + type);
                }
                streamCodec.encode(buf, provider);
            },
            buf -> {
                ResourceLocation type = buf.readResourceLocation();
                StreamCodec<RegistryFriendlyByteBuf, ? extends CelestialTransformProvider> streamCodec = STREAM_CODEC_REGISTRY.get(type);
                if (streamCodec == null) {
                    throw new IllegalArgumentException("Unknown CelestialTransformProvider stream codec for type: " + type);
                }
                return streamCodec.decode(buf);
            }
    );
}