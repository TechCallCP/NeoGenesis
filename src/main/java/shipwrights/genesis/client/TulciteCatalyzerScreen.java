package shipwrights.genesis.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.content.block.TulciteCatalyzerContainer;
import shipwrights.genesis.content.blockentity.TulciteCatalyzerBlockEntity;

@OnlyIn(Dist.CLIENT)
public class TulciteCatalyzerScreen extends AbstractContainerScreen<TulciteCatalyzerContainer> {

    private static final int ENERGY_LEFT = 36;
    private static final int ENERGY_WIDTH = 72;
    private static final int ENERGY_TOP = 44;
    private static final int ENERGY_HEIGHT = 8;

    private final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "textures/gui/tulcite_catalyzer.png");

    public TulciteCatalyzerScreen(TulciteCatalyzerContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.inventoryLabelY = this.imageHeight - 110;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        int power = menu.getPower();
        int p = (int) ((power / (float) TulciteCatalyzerBlockEntity.CAPACITY) * ENERGY_WIDTH);
        graphics.fillGradient(leftPos + ENERGY_LEFT, topPos + ENERGY_TOP, leftPos + ENERGY_LEFT + p, topPos + ENERGY_TOP + ENERGY_HEIGHT, 0xFF00FF00, 0xFF003300);
        graphics.fill(leftPos + ENERGY_LEFT + p, topPos + ENERGY_TOP, leftPos + ENERGY_LEFT + ENERGY_WIDTH, topPos + ENERGY_TOP + ENERGY_HEIGHT, 0xFF000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + ENERGY_LEFT && mouseX < leftPos + ENERGY_LEFT + ENERGY_WIDTH && mouseY >= topPos + ENERGY_TOP && mouseY < topPos + ENERGY_TOP + ENERGY_HEIGHT) {
            int power = menu.getPower();
            graphics.renderTooltip(this.font, Component.literal(power + " FE"), mouseX, mouseY);
        }
    }
}