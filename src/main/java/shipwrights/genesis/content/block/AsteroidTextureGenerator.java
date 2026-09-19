package shipwrights.genesis.content.block;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AsteroidTextureGenerator {

    public static void main(String[] args) {
        try {
            generate();
            System.out.println("Texture generation complete!");
        } catch (IOException e) {
            System.err.println("Error generating texture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final String moj_assets_path = "src/main/resources/assets/minecraft/textures/block/";
    private static final String genesis_assets_path = "src/main/resources/assets/neogenesis/textures/block/";

    public static void generate() throws IOException {
        String[] rockTextures = {
                moj_assets_path + "tuff.png",
                moj_assets_path + "deepslate_iron_ore.png",
                moj_assets_path + "deepslate_copper_ore.png",
                moj_assets_path + "deepslate.png",
        };

        generate("asteroid", 16, rockTextures);
    }

    public static void generate(String baseName, int gridSize, String[] texturePaths) throws IOException {
        AsteroidTextureLayout layout = AsteroidTextureLayout.generate(gridSize, texturePaths.length);
        int[][] grid = layout.data;

        int tileSize = 16;
        int imageSize = gridSize * tileSize;

        BufferedImage outputImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        BufferedImage[] sourceTextures = new BufferedImage[texturePaths.length];
        for (int i = 0; i < texturePaths.length; i++) {
            File sourceFile = new File(texturePaths[i]);
            if (sourceFile.exists()) {
                sourceTextures[i] = ImageIO.read(sourceFile);
                System.out.println("Loaded texture: " + texturePaths[i]);
            } else {
                System.err.println("Warning: Source texture not found: " + texturePaths[i]);
                sourceTextures[i] = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            }
        }

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int textureId = grid[y][x];
                BufferedImage sourceTile = sourceTextures[textureId % sourceTextures.length];

                for (int ty = 0; ty < tileSize; ty++) {
                    for (int tx = 0; tx < tileSize; tx++) {
                        int rgb = sourceTile.getRGB(tx, ty);
                        outputImage.setRGB(x * tileSize + tx, y * tileSize + ty, rgb);
                    }
                }
            }
        }

        String outputFilename = String.format("%s_combined.png", baseName);
        File outputFile = new File(new Random().nextInt(9999) + outputFilename);
        ImageIO.write(outputImage, "png", outputFile);
        System.out.println("Generated texture: " + outputFilename + " (" + imageSize + "x" + imageSize + " pixels)");
    }
}
