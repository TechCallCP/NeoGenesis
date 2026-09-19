package shipwrights.genesis.content.block.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shipwrights.genesis.NeoGenesisMod;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = NeoGenesisMod.MOD_ID)
public class BlockDataGenerator {

    public static String FOLDER = "src/main/resources/";
    private static final Logger log = LoggerFactory.getLogger(BlockDataGenerator.class);

    public static final Map<String, List<BlockType>> blocksToDatagen = Map.of(
            "voidstone", List.of(BlockType.simple, BlockType.slab, BlockType.stairs),
            "nullstone", List.of(BlockType.simple, BlockType.slab, BlockType.stairs),
            "riftrock", List.of(BlockType.simple, BlockType.slab, BlockType.stairs),
            "echostone", List.of(BlockType.simple, BlockType.slab, BlockType.stairs),
            "phaserock", List.of(BlockType.simple, BlockType.slab, BlockType.stairs),
            "warpstone", List.of(BlockType.simple, BlockType.slab, BlockType.stairs)
    );

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        runGeneration();
    }

    public static void main(String[] args) {
        runGeneration();
    }

    public static void runGeneration() {
        blocksToDatagen.forEach(BlockDataGenerator::generate);
    }

    private static void generate(String name, List<BlockType> types) {
        try {
            for (var type : types) {
                switch (type) {
                    case slab -> SlabGenerator.generateSlab(name);
                    case stairs -> StairGenerator.generateStair(name);
                    case pillar -> PillarGenerator.generatePillar(name);
                    default -> {}
                }
            }
        } catch (Exception e) {
            log.error("Error generating datagen for {}: ", name, e);
        }
    }
}
