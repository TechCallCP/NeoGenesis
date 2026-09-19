package shipwrights.genesis.content.fluid;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import shipwrights.genesis.NeoGenesisMod;

import java.util.function.Consumer;

/**
 * Fluid registration using Registrate to avoid circular dependency issues.
 * Registrate.create() automatically registers event listeners.
 */
public class GenesisFluids {

    public static final Registrate REGISTRATE = Registrate.create(NeoGenesisMod.MOD_ID);

    private static final ResourceLocation STILL_RL = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "block/miasma_still");
    private static final ResourceLocation FLOWING_RL = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "block/miasma_flow");
    private static final ResourceLocation OVERLAY_RL = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "block/miasma_overlay");
    private static final int TINT_COLOR = 0xCC8B9A32;

    public static final FluidEntry<BaseFlowingFluid.Flowing> MIASMA = REGISTRATE
            .fluid("miasma", STILL_RL, FLOWING_RL, GenesisFluids::createMiasmaFluidType, BaseFlowingFluid.Flowing::new)
            .lang("Miasma")
            .properties(p -> p
                    .density(0)
                    .viscosity(0)
                    .canConvertToSource(false)
                    .canPushEntity(false)
                    .fallDistanceModifier(1f)
                    .canSwim(false)
                    .canDrown(false)
                    .supportsBoating(false))
            .fluidProperties(p -> p
                    .levelDecreasePerBlock(2)
                    .slopeFindDistance(3)
                    .tickRate(10))
            .source(BaseFlowingFluid.Source::new)
            .register();

    /**
     * Factory method creating the FluidType with proper client-side textures and tints.
     */
    private static FluidType createMiasmaFluidType(FluidType.Properties properties) {
        return new FluidType(properties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return STILL_RL;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return FLOWING_RL;
                    }

                    @Override
                    public ResourceLocation getOverlayTexture() {
                        return OVERLAY_RL;
                    }

                    @Override
                    public int getTintColor() {
                        return TINT_COLOR;
                    }
                });
            }
        };
    }
}
