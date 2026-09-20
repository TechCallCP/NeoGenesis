package shipwrights.genesis.content.fluid;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import shipwrights.genesis.NeoGenesisMod;

public class GenesisFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NeoGenesisMod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, NeoGenesisMod.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, NeoGenesisMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, NeoGenesisMod.MOD_ID);

    private static final ResourceLocation STILL_RL = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "block/miasma_still");
    private static final ResourceLocation FLOWING_RL = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "block/miasma_flow");
    private static final ResourceLocation OVERLAY_RL = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "block/miasma_overlay");
    private static final int TINT_COLOR = 0xCC8B9A32;

    public static final DeferredHolder<FluidType, FluidType> MIASMA_FLUID_TYPE = FLUID_TYPES.register("miasma",
            () -> new FluidType(FluidType.Properties.create()
                    .density(0)
                    .viscosity(0)
                    .canConvertToSource(false)
                    .canPushEntity(false)
                    .fallDistanceModifier(1.0F)
                    .canSwim(false)
                    .canDrown(false)
                    .supportsBoating(false)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MIASMA_SOURCE = FLUIDS.register("miasma",
            () -> new BaseFlowingFluid.Source(GenesisFluids.MIASMA_PROPERTIES));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> MIASMA_FLOWING = FLUIDS.register("miasma_flowing",
            () -> new BaseFlowingFluid.Flowing(GenesisFluids.MIASMA_PROPERTIES));

    public static final DeferredHolder<Block, LiquidBlock> MIASMA_BLOCK = BLOCKS.register("miasma",
            () -> new LiquidBlock(MIASMA_SOURCE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));

    public static final DeferredHolder<Item, BucketItem> MIASMA_BUCKET = ITEMS.register("miasma_bucket",
            () -> new BucketItem(MIASMA_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties MIASMA_PROPERTIES = new BaseFlowingFluid.Properties(
            MIASMA_FLUID_TYPE,
            MIASMA_SOURCE,
            MIASMA_FLOWING
    )
            .slopeFindDistance(3)
            .levelDecreasePerBlock(2)
            .tickRate(10)
            .block(MIASMA_BLOCK)
            .bucket(MIASMA_BUCKET);

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            eventBus.addListener(GenesisFluids::onRegisterClientExtensions);
        }
    }

    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
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
        }, MIASMA_FLUID_TYPE.get());
    }
}
