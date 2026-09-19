package shipwrights.genesis.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TallMimicFeatherBlock extends TallFlowerBlock {
    public static final MapCodec<TallMimicFeatherBlock> CODEC = simpleCodec(TallMimicFeatherBlock::new);

    public TallMimicFeatherBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TallMimicFeatherBlock> codec() {
        return CODEC;
    }
}
