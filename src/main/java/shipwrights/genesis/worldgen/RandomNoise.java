package shipwrights.genesis.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

import org.jetbrains.annotations.NotNull;

public class RandomNoise implements DensityFunction {

    public static final MapCodec<RandomNoise> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("scale").forGetter(r -> r.scale),
                    DensityFunction.CODEC.fieldOf("frequency").forGetter(r -> r.frequency)
            ).apply(instance, RandomNoise::new)
    );

    public static final KeyDispatchDataCodec<RandomNoise> KEY_CODEC = KeyDispatchDataCodec.of(MAP_CODEC);

    private final double scale;
    private final Holder<DensityFunction> frequency;

    public RandomNoise(double scale, Holder<DensityFunction> frequency) {
        this.scale = scale;
        this.frequency = frequency;
    }

    @Override
    public double compute(@NotNull FunctionContext context) {
        double freqVal = this.frequency.value().compute(context) / 4.0;

        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();

        double center = getNoise(x, y, z);
        double currentScale = 1.0;

        if (getNoise(x + 25320, y * 4 - 3453, z * 2 + 534) > freqVal) return 0.0;

        if (center < getNoise(x + 1, y, z)) return 0.0;
        if (center < getNoise(x - 1, y, z)) return 0.0;
        if (center < getNoise(x, y + 1, z)) return 0.0;
        if (center < getNoise(x, y - 1, z)) return 0.0;
        if (center < getNoise(x, y, z + 1)) return 0.0;
        if (center < getNoise(x, y, z - 1)) return 0.0;

        double scaled = center * currentScale;
        return scaled > 0.1 ? scaled : 0.0;
    }

    private double getNoise(int x, int y, int z) {
        long seed = x * 3129871L ^ z * 116129781L ^ y * 42317861L;
        seed = seed * seed * 42317861L + seed * 11L;

        seed ^= (seed >>> 13);
        seed *= 0x5DEECE66DL;
        seed ^= (seed >>> 17);

        return (double) (seed & 0xFFFFFFF) / (double) 0xFFFFFFF;
    }

    @Override
    public void fillArray(double[] arr, ContextProvider provider) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = compute(provider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return this;
    }

    @Override
    public double minValue() {
        return -scale;
    }

    @Override
    public double maxValue() {
        return scale;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
