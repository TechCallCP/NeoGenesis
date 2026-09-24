package shipwrights.genesis.space.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface PlanetColorPalette {

    boolean isOverworld();

    int[] getRGB();

    record RGB(
            int r,
            int g,
            int b
    ) implements PlanetColorPalette {

        static final MapCodec<RGB> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("r").forGetter(RGB::r),
                Codec.INT.fieldOf("g").forGetter(RGB::g),
                Codec.INT.fieldOf("b").forGetter(RGB::b)
        ).apply(instance, RGB::new));

        public static final StreamCodec<ByteBuf, RGB> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, RGB::r,
                ByteBufCodecs.VAR_INT, RGB::g,
                ByteBufCodecs.VAR_INT, RGB::b,
                RGB::new
        );

        @Override
        public boolean isOverworld() {
            return false;
        }

        @Override
        public int[] getRGB() {
            return new int[]{r, g, b};
        }
    }

    class Overworld implements PlanetColorPalette {

        static final MapCodec<Overworld> MAP_CODEC = MapCodec.unit(new Overworld());
        public static final StreamCodec<ByteBuf, Overworld> STREAM_CODEC = StreamCodec.unit(new Overworld());

        @Override
        public boolean isOverworld() {
            return true;
        }

        @Override
        public int[] getRGB() {
            throw new UnsupportedOperationException("Overworld should be handled differently");
        }
    }

    Codec<PlanetColorPalette> CODEC = Codec.STRING.dispatch(
            "type",
            palette -> palette.isOverworld() ? "neogenesis:overworld" : "neogenesis:rgb",
            type -> switch (type) {
                case "neogenesis:rgb" -> RGB.MAP_CODEC;
                case "neogenesis:overworld" -> Overworld.MAP_CODEC;
                default -> throw new IllegalArgumentException("Unknown PlanetColorPalette type: " + type);
            }
    );

    StreamCodec<ByteBuf, PlanetColorPalette> STREAM_CODEC = StreamCodec.of(
            (buf, palette) -> {
                if (palette.isOverworld()) {
                    buf.writeByte(0);
                } else if (palette instanceof RGB rgb) {
                    buf.writeByte(1);
                    RGB.STREAM_CODEC.encode(buf, rgb);
                }
            },
            buf -> {
                byte type = buf.readByte();
                return switch (type) {
                    case 0 -> new Overworld();
                    case 1 -> RGB.STREAM_CODEC.decode(buf);
                    default -> throw new IllegalArgumentException("Unknown PlanetColorPalette type ID: " + type);
                };
            }
    );
}