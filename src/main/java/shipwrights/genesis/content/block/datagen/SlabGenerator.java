package shipwrights.genesis.content.block.datagen;

import shipwrights.genesis.NeoGenesisMod;

public class SlabGenerator {

    public static void generateSlab(String name) {
        generateBlockState(name);
        generateBlockModel(name);
        generateLootTable(name);
        BlockItemGenerator.generate(name + "_slab");
    }

    private static void generateBlockModel(String name) {
        generateSlabModel(name);
        generateTopSlabModel(name);
    }

    private static void generateSlabModel(String name) {
        String json = """
              {
                "parent": "minecraft:block/slab",
                "textures": {
                  "bottom": "%1$s:block/%2$s",
                  "top": "%1$s:block/%2$s",
                  "side": "%1$s:block/%2$s"
                }
              }
              """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "assets/" + NeoGenesisMod.MOD_ID + "/models/block/" + name + "_slab.json";

        FileWriter.writeFile(path, json);
    }

    private static void generateTopSlabModel(String name) {
        String json = """
              {
                "parent": "minecraft:block/slab_top",
                "textures": {
                  "bottom": "%1$s:block/%2$s",
                  "top": "%1$s:block/%2$s",
                  "side": "%1$s:block/%2$s"
                }
              }
              """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "assets/" + NeoGenesisMod.MOD_ID + "/models/block/" + name + "_slab_top.json";

        FileWriter.writeFile(path, json);
    }

    private static void generateBlockState(String name) {
        String json = """
                {
                  "variants": {
                    "type=bottom": {
                      "model": "%1$s:block/%2$s_slab"
                    },
                    "type=top": {
                      "model": "%1$s:block/%2$s_slab_top"
                    },
                    "type=double": {
                      "model": "%1$s:block/%2$s"
                    }
                  }
                }
              """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "assets/" + NeoGenesisMod.MOD_ID + "/blockstates/" + name + "_slab.json";

        FileWriter.writeFile(path, json);
    }

    private static void generateLootTable(String name) {
        String json = """
                {
                  "type": "minecraft:block",
                  "pools": [
                    {
                      "rolls": 1.0,
                      "entries": [
                        {
                          "type": "minecraft:item",
                          "name": "%1$s:%2$s_slab",
                          "functions": [
                            {
                              "function": "minecraft:set_count",
                              "conditions": [
                                {
                                  "condition": "minecraft:block_state_property",
                                  "block": "%1$s:%2$s_slab",
                                  "properties": {
                                    "type": "double"
                                  }
                                }
                              ],
                              "count": 2.0,
                              "add": false
                            },
                            {
                              "function": "minecraft:explosion_decay"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              """.formatted(NeoGenesisMod.MOD_ID, name);
        String path = BlockDataGenerator.FOLDER + "data/" + NeoGenesisMod.MOD_ID + "/loot_table/blocks/" + name + "_slab.json";

        FileWriter.writeFile(path, json);
    }
}
