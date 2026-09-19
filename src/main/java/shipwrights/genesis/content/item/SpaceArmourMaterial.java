package shipwrights.genesis.content.item;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import shipwrights.genesis.NeoGenesisMod;

import java.util.EnumMap;
import java.util.List;

public class SpaceArmourMaterial {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, NeoGenesisMod.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SPACE_SUIT = ARMOR_MATERIALS.register("space_suit",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 1);
                        map.put(ArmorItem.Type.LEGGINGS, 1);
                        map.put(ArmorItem.Type.CHESTPLATE, 1);
                        map.put(ArmorItem.Type.HELMET, 1);
                        map.put(ArmorItem.Type.BODY, 1);
                    }),
                    5,
                    SoundEvents.WOOL_HIT,
                    () -> Ingredient.of(Items.PHANTOM_MEMBRANE),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "space_suit"))),
                    1.0F,
                    0.0F
            ));
}
