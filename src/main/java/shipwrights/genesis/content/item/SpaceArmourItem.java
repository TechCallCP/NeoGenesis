package shipwrights.genesis.content.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class SpaceArmourItem extends ArmorItem {

    public SpaceArmourItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public static boolean hasModule(ItemStack armour, String moduleName) {
        CustomData customData = armour.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().contains(moduleName);
    }

    public static void addModule(ItemStack armour, String moduleName) {
        CustomData.update(DataComponents.CUSTOM_DATA, armour, tag -> tag.putBoolean(moduleName, true));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("has_cooling")) {
                tooltipComponents.add(Component.literal("Has Cooling"));
            }
            if (tag.contains("has_heat")) {
                tooltipComponents.add(Component.literal("Has heat"));
            }
            if (tag.contains("has_oxygen")) {
                tooltipComponents.add(Component.literal("Has Oxygen"));
            }
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
