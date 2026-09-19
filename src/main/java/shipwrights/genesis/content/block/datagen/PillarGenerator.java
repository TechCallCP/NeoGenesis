package shipwrights.genesis.content.block.datagen;

import shipwrights.genesis.NeoGenesisMod;

public class PillarGenerator {

    public static void generatePillar(String name) {
        generateBlockState(name);
        generateBlockModel(name);
        generateLootTable(name);
        BlockItemGenerator.generate(name + "_pillar");
    }

    private static void generateBlockModel(String name) {
        String json = """
                {
                  "parent": "minecraft:block/cube_column",
                  "textures": {
                    "end": "%1$s:block/%2$s_top",
                    "side": "%1$s:block/%2$s"
                  }
                }
              """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "assets/" + NeoGenesisMod.MOD_ID + "/models/block/" + name + "_pillar.json";

        FileWriter.writeFile(path, json);
    }

    private static void generateBlockState(String name) {
        String json = """
                {
                  "variants": {
                    "axis=x": {
                      "model": "%1$s:block/%2$s_pillar",
                      "x": 90,
                      "y": 90
                    },
                    "axis=y": {
                      "model": "%1$s:block/%2$s_pillar"
                    },
                    "axis=z": {
                      "model": "%1$s:block/%2$s_pillar",
                      "x": 90
                    }
                  }
                }
              """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "assets/" + NeoGenesisMod.MOD_ID + "/blockstates/" + name + "_pillar.json";

        FileWriter.writeFile(path, json);
    }

    private static void generateLootTable(String name) {
        String json = """
                {
                  "type": "minecraft:block",
                  "pools": [
                    {
                      "bonus_rolls": 0.0,
                      "rolls": 1.0,
                      "entries": [
                        {
                          "type": "minecraft:item",
                          "name": "%1$s:%2$s_pillar"
                        }
                      ],
                      "conditions": [
                        {
                          "condition": "minecraft:survives_explosion"
                        }
                      ]
                    }
                  ]
                }
              """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "data/" + NeoGenesisMod.MOD_ID + "/loot_table/blocks/" + name + "_pillar.json";

        FileWriter.writeFile(path, json);
    }
}
