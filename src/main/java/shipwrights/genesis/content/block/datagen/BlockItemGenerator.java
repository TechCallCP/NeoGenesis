package shipwrights.genesis.content.block.datagen;

import shipwrights.genesis.NeoGenesisMod;

public class BlockItemGenerator {

    public static void generate(String name) {
        String json = """
            {
              "parent": "%1$s:block/%2$s"
            }
            """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "assets/" + NeoGenesisMod.MOD_ID + "/models/item/" + name + ".json";

        FileWriter.writeFile(path, json);
    }
}
