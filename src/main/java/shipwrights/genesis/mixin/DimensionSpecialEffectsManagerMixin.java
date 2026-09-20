package shipwrights.genesis.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import shipwrights.genesis.NeoGenesisMod;

@Mixin(value = shipwrights.genesis.mixin.DimensionSpecialEffectsManager.class, remap = false)
public class DimensionSpecialEffectsManagerMixin {

    @Shadow
    private static ImmutableMap<ResourceLocation, DimensionSpecialEffects> EFFECTS;

    @WrapMethod(method = "getForType")
    private static DimensionSpecialEffects wrapGetForType(ResourceLocation type, Operation<DimensionSpecialEffects> original) {
        if (type.equals(ResourceLocation.parse("minecraft:overworld"))) {
            return EFFECTS.get(NeoGenesisMod.GENERIC_PLANET_ID);
        }
        return original.call(type);
    }
}
